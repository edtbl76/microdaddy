package org.tbl.microdaddy.composite.product.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.product.ProductService;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.recommendation.RecommendationService;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.core.review.ReviewService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.api.exceptions.NotFoundException;
import org.tbl.microdaddy.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
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

        this.restTemplate = restTemplate;
        this.mapper = mapper;

        this.productServiceUrl = String.format("http://%s:%s/product", productServiceHost, productServicePort);
        this.recommendationServiceUrl = String.format("http://%s:%s/recommendation",
                recommendationServiceHost, recommendationServicePort);
        this.reviewServiceUrl= String.format("http://%s:%s/review", reviewServiceHost,reviewServicePort);
    }


    @Override
    public Product createProduct(Product body) {
        try {
            String url = this.productServiceUrl;
            log.debug("Calling createProduct endpoint at URL: " + url);

            Product product = restTemplate.postForObject(url, body, Product.class);
            log.debug("Created product with id: {}", product.productId());

            return product;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = this.productServiceUrl + "/" + productId;
            log.debug("Calling getProduct endpoint at URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            log.debug("Found product with id: {}", product.productId());

            return product;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
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
            log.warn("Created a recommendation with product id: {}", recommendation.productId());

            return recommendation;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        try {
            String url = this.recommendationServiceUrl + "?productId=" + productId;
            log.debug("Calling getRecommendations endpoint on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();

            log.debug("Found {} recommendations for product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (RuntimeException e) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}",
                    e.getMessage());
            return new ArrayList<>();
        }
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
            log.warn("Created a review with product id: {}", review.productId());

            return review;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = this.reviewServiceUrl + "?productId=" + productId;
            log.debug("Calling getReviews endpoint at URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
                    .getBody();

            log.debug("Found {} reviews for product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (RuntimeException e) {
            log.warn("Got an exception while requesting reviews, return zero reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
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
    private RuntimeException handleHttpClientException(HttpClientErrorException exception) {
        HttpStatusCode statusCode = exception.getStatusCode();
        if (statusCode.equals(NOT_FOUND)) {
            throw new NotFoundException(getErrorMessage(exception));
        } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
            throw new InvalidInputException(getErrorMessage(exception));
        } else {
            log.warn("Got an unexpected HTTP error: {}, rethrowing.", exception.getStatusCode());
            log.warn("Error Body: {}", exception.getResponseBodyAsString());
            return exception;
        }
    }

    private String getErrorMessage(HttpClientErrorException e) {
        try {
            return mapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ex) {
            return e.getMessage();
        }
    }
}
