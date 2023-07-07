package org.tbl.microdaddy.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.product.ProductService;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.recommendation.RecommendationService;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.core.review.ReviewService;
import org.tbl.microdaddy.api.event.Event;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.api.exceptions.NotFoundException;
import org.tbl.microdaddy.util.http.HttpErrorInfo;
import org.tbl.microdaddy.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.net.URI;

import static java.util.logging.Level.FINE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.tbl.microdaddy.api.event.Event.Type.CREATE;
import static org.tbl.microdaddy.api.event.Event.Type.DELETE;
import static reactor.core.publisher.Flux.empty;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";


    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final Scheduler publishEventScheduler;
    private final StreamBridge streamBridge;

    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductCompositeIntegration(
            // Had to set this to @Lazy to solve the circular dependency e/
            // product composite service application.
            @Qualifier("publishEventScheduler") @Lazy Scheduler publishEventScheduler,
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            ServiceUtil serviceUtil) {

        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClientBuilder.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;
        this.serviceUtil = serviceUtil;
    }


    @Observed(
            name = "createProduct",
            contextualName = "product-composite-integration.create-product"
    )
    @Override
    public Mono<Product> createProduct(Product body) {

        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);

    }

    @Observed(
            name = "getProduct",
            contextualName = "product-composite-integration.get-product"
    )
    @Override
    @Retry(name = "product")
    @TimeLimiter(name = "product")
    @CircuitBreaker(name = "product", fallbackMethod = "getProductFallbackValue")
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {

        URI url = UriComponentsBuilder
                .fromUriString(PRODUCT_SERVICE_URL +
                        "/product/{productId}?delay={delay}&faultPercent{faultPercent}")
                        .build(productId, delay, faultPercent);

        log.info("Calling getProduct endpoint at URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log(log.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);

    }

    private Mono<Product> getProductFallbackValue(
            int productId, int delay, int faultPercent, CallNotPermittedException ex) {

        log.warn("Creating a fail-fast product for productId = {}, delay = {}, faultPercent = {} and exception = {}",
                productId, delay, faultPercent, ex.toString());

        if (productId == 13) {
            String errorMessage = "Product Id: " + productId + " not found in fallback cache.";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        return Mono.just(new Product(
                productId,
                "Fallback product" + productId,
                productId,
                serviceUtil.getServiceAddress()
        ));
    }

    @Observed(
            name = "deleteProduct",
            contextualName = "product-composite-integration.delete-product"
    )
    @Override
    public Mono<Void> deleteProduct(int productId) {

        return Mono.fromRunnable(() -> sendMessage("products-out-0",
                        new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();

    }

    @Observed(
            name = "createRecommendation",
            contextualName = "product-composite-integration.create-recommendation"
    )
    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);

    }

    @Observed(
            name = "getRecommendations",
            contextualName = "product-composite-integration.get-recommendations"
    )
    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        URI url = UriComponentsBuilder
                .fromUriString(RECOMMENDATION_SERVICE_URL + "/recommendation?productId={productId}")
                .build(productId);
        log.debug("Calling getRecommendations endpoint on URL: {}", url);

        // returns an empty result so composite supports partial results if something happens during the
        // call to recommendation service
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());

    }


    @Observed(
            name = "deleteRecommendations",
            contextualName = "product-composite-integration.delete-recommendations"
    )
    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0",
                        new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();

    }

    @Observed(
            name = "createReview",
            contextualName = "product-composite-integration.create-review"
    )
    @Override
    public Mono<Review> createReview(Review body) {

        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);

    }

    @Observed(
            name = "getReviews",
            contextualName = "product-composite-integration.get-reviews"
    )
    @Override
    public Flux<Review> getReviews(int productId) {

        URI url = UriComponentsBuilder
                .fromUriString(REVIEW_SERVICE_URL + "/review?productId={productId}")
                        .build(productId);
        log.info("Calling getReviews endpoint at URL: {}", url);


        // returns an empty result so composite supports partial results if something happens during the
        // call to recommendation service
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());

    }

    @Observed(
            name = "deleteReviews",
            contextualName = "product-composite-integration.delete-reviews"
    )
    @Override
    public Mono<Void> deleteReviews(int productId) {

        return Mono.fromRunnable(() -> sendMessage("reviews-out-0",
                        new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }

    @Observed(
            name = "sendMessage",
            contextualName = "product-composite-integration.send-message"
    )
    private void sendMessage(String bindingName, Event event) {
        log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable throwable) {

        if(!(throwable instanceof WebClientResponseException ex)) {
            log.warn("Unexpected error: {}, rethrowing", throwable.toString());
            return throwable;
        }

        HttpStatusCode statusCode = ex.getStatusCode();
        if (statusCode.equals(NOT_FOUND)) {
            throw new NotFoundException(getErrorMessage(ex));
        } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
            throw new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Unexpected error: {}, rethrowing", ex.getResponseBodyAsString());
        log.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;

    }

    private String getErrorMessage(WebClientResponseException exception) {
        try {
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ex) {
            return exception.getMessage();
        }
    }
}
