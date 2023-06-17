package org.tbl.microdaddy.core.review.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.core.review.ReviewService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.core.review.persistence.ReviewEntity;
import org.tbl.microdaddy.core.review.persistence.ReviewRepository;
import org.tbl.microdaddy.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(
            @Qualifier("jdbcScheduler") Scheduler jdbcScheduler,
            ReviewRepository repository,
            ReviewMapper mapper,
            ServiceUtil serviceUtil) {
        this.jdbcScheduler = jdbcScheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Mono<Review> createReview(Review body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        return Mono.fromCallable(() -> blockingCreateReview(body))
                .subscribeOn(jdbcScheduler);

    }

    private Review blockingCreateReview(Review body) {

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
    public Flux<Review> getReviews(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.info("Calling getReviews for product with id={}", productId);

        return Mono.fromCallable(() -> blockingGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(log.getName(), FINE)
                .subscribeOn(jdbcScheduler);
    }

    private List<Review> blockingGetReviews(int productId) {

        List<ReviewEntity> entities = repository.findByProductId(productId);
        List<Review> reviews = mapper.entityListToApiList(entities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("Response size: {}", reviews.size());

        return reviews;

    }

    @Override
    public Mono<Void> deleteReviews(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromRunnable(() -> blockingDeleteReviews(productId))
                .subscribeOn(jdbcScheduler)
                .then();
    }

    private void blockingDeleteReviews(int productId) {
        log.debug("deleteReviews: attempts to delete reviews for product w/ productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
