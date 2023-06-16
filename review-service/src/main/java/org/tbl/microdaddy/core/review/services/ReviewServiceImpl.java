package org.tbl.microdaddy.core.review.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.core.review.ReviewService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.core.review.persistence.ReviewEntity;
import org.tbl.microdaddy.core.review.persistence.ReviewRepository;
import org.tbl.microdaddy.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(
            ReviewRepository repository,
            ReviewMapper mapper,
            ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity savedEntity = repository.save(entity);

            log.debug("createReview: created a review entity {}/{}",
                    body.getProductId(), body.getReviewId());
            return mapper.entityToApi(savedEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new InvalidInputException(
                    "Duplicate key, Product Id: "
                    + body.getProductId()
                    + ", Review Id:"
                    + body.getReviewId()
            );
        }

    }

    @Override
    public List<Review> getReviews(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> entities = repository.findByProductId(productId);
        List<Review> reviews = mapper.entityListToApiList(entities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getReviews: response size: {}", reviews.size());

        return reviews;

    }

    @Override
    public void deleteReviews(int productId) {
        log.debug("deleteReviews: attempts to delete reviews for product w/ productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
