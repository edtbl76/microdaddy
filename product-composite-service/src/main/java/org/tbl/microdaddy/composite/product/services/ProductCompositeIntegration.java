package org.tbl.microdaddy.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.product.ProductService;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.recommendation.RecommendationService;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.core.review.ReviewService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.api.exceptions.NotFoundException;
import org.tbl.microdaddy.util.http.HttpErrorInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static java.util.logging.Level.FINE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static reactor.core.publisher.Flux.empty;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            WebClient.Builder webClient,
            ObjectMapper mapper,

            @Value("${app.product-service.host}")
            String productServiceHost,
            @Value("${app.product-service.port}")
            String productServicePort,

            @Value("${app.recommendation-service.host}")
            String recommendationServiceHost,
            @Value("${app.recommendation-service.port}")
            String recommendationServicePort,

            @Value("${app.review-service.host}")
            String reviewServiceHost,
            @Value("${app.review-service.port}")
            String reviewServicePort) {

        this.webClient = webClient.build();
        this.mapper = mapper;

        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
    }


    @Override
    public Product createProduct(Product body) {

        try {
            String url = this.productServiceUrl;
            log.debug("Calling createProduct endpoint at URL: " + url);

            Product product = restTemplate.postForObject(url, body, Product.class);
            log.debug("Created product with id: {}", product.getProductId());

            return product;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Mono<Product> getProduct(int productId) {

        String url = this.productServiceUrl + "/product/" + productId;
        log.debug("Calling getProduct endpoint at URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log(log.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);

    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = this.productServiceUrl + "/" + productId;
            log.debug("Calling deleteProduct endpoint at URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    @Override
    public Recommendation createRecommendation(Recommendation body) {

        try {
            String url = this.recommendationServiceUrl;
            log.debug("Calling createRecommendation endpoint at URL: {}", url);

            Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
            log.warn("Created a recommendation with product id: {}", recommendation.getProductId());

            return recommendation;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        String url = this.recommendationServiceUrl + "/recommendation?productId=" + productId;
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


    @Override
    public void deleteRecommendations(int productId) {

        try {
            String url = this.recommendationServiceUrl + "?productId=" + productId;
            log.debug("Calling deleteRecommendations endpoint on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    @Override
    public Review createReview(Review body) {

        try {
            String url = this.reviewServiceUrl;
            log.debug("Calling createReview endpoint at URL: {}", url);

            Review review = restTemplate.postForObject(url, body, Review.class);
            log.warn("Created a review with product id: {}", review.getProductId());

            return review;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        String url = this.reviewServiceUrl + "?productId=" + productId;
        log.debug("Calling getReviews endpoint at URL: {}", url);


        // returns an empty result so composite supports partial results if something happens during the
        // call to recommendation service
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());

    }


    @Override
    public void deleteReviews(int productId) {

        try {
            String url = this.reviewServiceUrl + "?productId=" + productId;
            log.debug("Calling deleteReviews endpoint on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    // Helpers
    private Throwable handleException(Throwable throwable) {

        if(!(throwable instanceof WebClientResponseException ex)) {
            log.warn("Unexpected error: {}, rethrowing", throwable.toString());
            return throwable;
        }

        switch (ex.getStatusCode()) {
            case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
            default -> {
                log.warn("Unexpected error: {}, rethrowing", ex.getResponseBodyAsString());
                log.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
            }
        }
    }

    private String getErrorMessage(WebClientResponseException exception) {
        try {
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ex) {
            return exception.getMessage();
        }
    }
}
