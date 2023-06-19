package org.tbl.microdaddy.composite.product.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.composite.product.ProductAggregate;
import org.tbl.microdaddy.api.composite.product.ProductCompositeService;
import org.tbl.microdaddy.api.composite.product.RecommendationSummary;
import org.tbl.microdaddy.api.composite.product.ReviewSummary;
import org.tbl.microdaddy.api.composite.product.ServiceAddresses;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.util.http.ServiceUtil;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.FINE;

@Slf4j
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public Mono<Void> createProduct(ProductAggregate body) {

        try {
            List<Mono> monos = new ArrayList<>();
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.productId());

            Product product = new Product(body.productId(), body.name(), body.weight(), null);
            monos.add(integration.createProduct(product));

            if (body.recommendations() != null) {
                body.recommendations().forEach(recommendationSummary-> {
                    Recommendation recommendation = new Recommendation(
                            body.productId(),
                            recommendationSummary.recommendationId(),
                            recommendationSummary.author(),
                            recommendationSummary.rate(),
                            recommendationSummary.content(),
                            null);
                    monos.add(integration.createRecommendation(recommendation));
                });
            }

            if (body.reviews() != null) {
                body.reviews().forEach(reviewSummary -> {
                    Review review = new Review(
                            body.productId(),
                            reviewSummary.reviewId(),
                            reviewSummary.author(),
                            reviewSummary.subject(),
                            reviewSummary.content(),
                            null);
                    monos.add(integration.createReview(review));
                });

            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.productId());

            return Mono.zip(objects -> "", monos.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();

        } catch (RuntimeException ex) {
            log.warn("createCompositeProduct failed: {}", ex.toString());
            throw ex;
        }
    }

    @Override
    public Mono<ProductAggregate> getProduct(int productId) {

        log.info("calling getCompositeProduct for product wth id: {}", productId);

        return Mono.zip(objects -> createProductAggregate(
                (Product) objects[0],
                (List<Recommendation>) objects[1],
                (List<Review>) objects[2],
                serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log(log.getName(), FINE);

    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        try {
            log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

            return Mono.zip(objects -> "",
                            integration.deleteProduct(productId),
                            integration.deleteRecommendations(productId),
                            integration.deleteReviews(productId))
                    .doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                    .log(log.getName(), FINE)
                    .then();
        } catch (RuntimeException ex) {
            log.warn("deleteCompositeProduct failed: {}", ex.toString());
            throw ex;
        }
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // product info
        int productId = product.getProductId();
        String productName = product.getName();
        int productWeight = product.getWeight();

        // summarize recommendations
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(recommendation -> new RecommendationSummary(
                                recommendation.getRecommendationId(),
                                recommendation.getAuthor(),
                                recommendation.getRate(),
                                recommendation.getContent()
                        ))
                        .toList();

        // summarize reviews
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(review -> new ReviewSummary(
                                review.getReviewId(),
                                review.getAuthor(),
                                review.getSubject(),
                                review.getContent()
                        ))
                        .toList();


        // get addresses and aggregate them
        String productAddress = product.getServiceAddress();
        String recommendationAddress = (recommendations != null && recommendations.size() > 0)
                ? recommendations.get(0).getServiceAddress() : "";
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(
                serviceAddress,
                productAddress,
                reviewAddress,
                recommendationAddress
        );

        return new ProductAggregate(
                productId,
                productName,
                productWeight,
                recommendationSummaries,
                reviewSummaries,
                serviceAddresses);
    }
}
