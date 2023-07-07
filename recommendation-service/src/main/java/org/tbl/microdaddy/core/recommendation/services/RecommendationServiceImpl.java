package org.tbl.microdaddy.core.recommendation.services;

import io.micrometer.observation.annotation.Observed;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static java.util.logging.Level.FINE;

@RestController
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private static final String INVALID_PRODUCT_ID = "Invalid productId: ";

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

    @Observed(
            name = "createRecommendation",
            contextualName = "recommendation-service.create-recommendation"
    )
    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException(INVALID_PRODUCT_ID + body.getProductId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()
                                + ", Recommendation Id:" + body.getRecommendationId()))
                .map(mapper::entityToApi);

    }

    @Observed(
            name = "getRecommendations",
            contextualName = "recommendation-service.get-recommendations"
    )
    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException(INVALID_PRODUCT_ID + productId);
        }

        log.info("calling getRecommendations for product with id={}", productId);

        return repository.findByProductId(productId)
                .log(log.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);

    }

    @Observed(
            name = "deleteRecommendations",
            contextualName = "recommendation-service.delete-recommendations"
    )
    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException(INVALID_PRODUCT_ID + productId);
        }

        log.debug("deleteRecommendations: attempting to delete recommendations for product with productId: {}",
                productId);

        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation recommendation) {
        recommendation.setServiceAddress(serviceUtil.getServiceAddress());
        return recommendation;
    }
}
