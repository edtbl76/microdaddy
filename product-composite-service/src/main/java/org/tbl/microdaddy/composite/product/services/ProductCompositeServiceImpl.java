package org.tbl.microdaddy.composite.product.services;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.FINE;
import static org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext;

@Slf4j
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final String NO_JWT_TESTING_MSG = "No JWT based Authentication supplied. Are we running tests??";

    private final SecurityContext nullSecurityContext = new SecurityContextImpl();
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Observed(name = "createProduct", contextualName = "productComposite#createProduct")
    @Override
    public Mono<Void> createProduct(ProductAggregate body) {

        try {
            List<Mono> monos = new ArrayList<>();

            monos.add(getLogAuthorizationInfoMono());

            log.info("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            monos.add(integration.createProduct(product));

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(recommendationSummary-> {
                    Recommendation recommendation = new Recommendation(
                            body.getProductId(),
                            recommendationSummary.getRecommendationId(),
                            recommendationSummary.getAuthor(),
                            recommendationSummary.getRate(),
                            recommendationSummary.getContent(),
                            null);
                    monos.add(integration.createRecommendation(recommendation));
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(reviewSummary -> {
                    Review review = new Review(
                            body.getProductId(),
                            reviewSummary.getReviewId(),
                            reviewSummary.getAuthor(),
                            reviewSummary.getSubject(),
                            reviewSummary.getContent(),
                            null);
                    monos.add(integration.createReview(review));
                });

            }

            log.info("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

            return Mono.zip(objects -> "", monos.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();

        } catch (RuntimeException ex) {
            log.warn("createCompositeProduct failed: {}", ex.toString());
            throw ex;
        }
    }

    @Observed(
            name = "getProduct",
            contextualName = "productComposite#getProduct")
    @Override
    public Mono<ProductAggregate> getProduct(int productId, int delay, int faultPercent) {

        log.info("calling getCompositeProduct for product wth id: {}", productId);

        return Mono.zip(objects -> createProductAggregate(
                (SecurityContext) objects[0],
                (Product) objects[1],
                (List<Recommendation>) objects[2],
                (List<Review>) objects[3],
                serviceUtil.getServiceAddress()),
                getSecurityContextMono(),
                integration.getProduct(productId, delay, faultPercent),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log(log.getName(), FINE);

    }

    @Observed(
            name = "deleteProduct",
            contextualName = "productComposite#delete-product"
    )
    @Override
    public Mono<Void> deleteProduct(int productId) {
        try {
            log.info("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);


            return Mono.zip(objects -> "",
                            getLogAuthorizationInfoMono(),
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

    @Observed(name = "createProductAggregate", contextualName = "productComposite#create-product-aggregate")
    private ProductAggregate createProductAggregate(
            SecurityContext securityContext,
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        logAuthorizationInfo(securityContext);

        // product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

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
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0)
                ? recommendations.get(0).getServiceAddress() : "";

        ServiceAddresses serviceAddresses = new ServiceAddresses(
                serviceAddress,
                productAddress,
                reviewAddress,
                recommendationAddress
        );

        return new ProductAggregate(
                productId,
                name,
                weight,
                recommendationSummaries,
                reviewSummaries,
                serviceAddresses);
    }

    private Mono<SecurityContext> getLogAuthorizationInfoMono() {
        return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
    }

    private Mono<SecurityContext> getSecurityContextMono() {
        return getContext().defaultIfEmpty(nullSecurityContext);
    }

    // Hard to read, but quite nice if it becomes part of the lexicon.
    private void logAuthorizationInfo(SecurityContext securityContext) {
        if (securityContext != null
                && securityContext.getAuthentication() != null
                && securityContext.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Jwt token = jwtAuthenticationToken.getToken();
            logAuthorizationInfo(token);
        } else {
            log.warn(NO_JWT_TESTING_MSG);
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            log.warn(NO_JWT_TESTING_MSG);
        } else {
            if (log.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getSubject();
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getExpiresAt();


                log.debug("Authorization info: Subject: {}, scopes: {}, expires: {}, issuer: {}, audience: {}",
                        subject,
                        scopes,
                        expires,
                        issuer,
                        audience);

//                  set for debugging.
                log.debug("JWT Headers: {}", jwt.getHeaders());
                log.debug("JWT Claims: {}", jwt.getClaims());
            }
        }
    }
}
