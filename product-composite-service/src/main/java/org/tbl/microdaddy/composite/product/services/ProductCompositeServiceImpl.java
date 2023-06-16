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
import org.tbl.microdaddy.api.exceptions.NotFoundException;
import org.tbl.microdaddy.util.http.ServiceUtil;

import java.util.List;

@Slf4j
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createProduct(ProductAggregate body) {

        try {
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.productId());

            Product product = new Product(body.productId(), body.name(), body.weight(), null);
            integration.createProduct(product);

            if (body.recommendations() != null) {
                body.recommendations().forEach(recommendationSummary-> {
                    Recommendation recommendation = new Recommendation(
                            body.productId(),
                            recommendationSummary.recommendationId(),
                            recommendationSummary.author(),
                            recommendationSummary.rate(),
                            recommendationSummary.content(),
                            null);
                    integration.createRecommendation(recommendation);
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
                    integration.createReview(review);
                });

            }
        } catch (RuntimeException ex) {
            log.warn("createCompositeProduct failed", ex);
            throw ex;
        }
    }

    @Override
    public ProductAggregate getProduct(int productId) {

        log.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);
        List<Review> reviews = integration.getReviews(productId);
        log.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void deleteProduct(int productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);
        integration.deleteRecommendations(productId);
        integration.deleteReviews(productId);

        log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);

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
