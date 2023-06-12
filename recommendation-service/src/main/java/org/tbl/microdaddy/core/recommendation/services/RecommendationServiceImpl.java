package org.tbl.microdaddy.core.recommendation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.recommendation.RecommendationService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        // TODO impl db to replace simulation code
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 113) {
            log.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Recommendation> list = new ArrayList<>();
        list.add(new Recommendation(
                productId,
                1,
                "Author 1",
                1,
                "Content 1",
                serviceUtil.getServiceAddress()));
        list.add(new Recommendation(
                productId,
                2,
                "Author 2",
                2,
                "Content 2",
                serviceUtil.getServiceAddress()));
        list.add(new Recommendation(
                productId,
                3,
                "Author 3",
                3,
                "Content 3",
                serviceUtil.getServiceAddress()));

        log.debug("/recommendation response size: {}", list.size());
        return list;
    }
}
