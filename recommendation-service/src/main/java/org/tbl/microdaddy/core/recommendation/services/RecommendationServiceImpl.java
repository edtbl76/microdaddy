package org.tbl.microdaddy.core.recommendation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.recommendation.RecommendationService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.core.recommendation.persistence.RecommendationEntity;
import org.tbl.microdaddy.core.recommendation.persistence.RecommendationRepository;
import org.tbl.microdaddy.util.http.ServiceUtil;

import java.util.List;

@RestController
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(
            RecommendationRepository repository,
            RecommendationMapper mapper,
            ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity savedEntity = repository.save(entity);

            log.debug("createRecommendation: created a recommendation entity: {}/{}",
                    body.getProductId(),
                    body.getRecommendationId());

            return mapper.entityToApi(savedEntity);
        } catch (DuplicateKeyException e) {
            throw new InvalidInputException(
                    "Duplicate key, Product Id: " + body.getProductId()
                    + ", Recommendation Id:" + body.getRecommendationId()
            );
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entities = repository.findByProductId(productId);
        List<Recommendation> recommendations = mapper.entityListToApiList(entities);
        recommendations.forEach(recommendation -> recommendation.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getRecommendations: response size: {}", recommendations.size());
        return recommendations;
    }

    @Override
    public void deleteRecommendations(int productId) {
        log.debug("deleteRecommendations: attempting to delete recommendations for product with productId: {}",
                productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
