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

        this.productServiceUrl = String.format("http://%s:%s/product/", productServiceHost, productServicePort);
        this.recommendationServiceUrl = String.format("http://%s:%s/recommendation?productId=",
                recommendationServiceHost, recommendationServicePort);
        this.reviewServiceUrl= String.format("http://%s:%s/review?productId=",
                reviewServiceHost,reviewServicePort);
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = this.productServiceUrl + productId;
            log.debug("Calling getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            /// TODO handle null
            log.debug("Found product with id: {}", product.productId());

            return product;
        } catch (HttpClientErrorException e) {

            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode.equals(NOT_FOUND)) {
                throw new NotFoundException(getErrorMessage(e));
            } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
                throw new InvalidInputException(getErrorMessage(e));
            }
            log.warn("Got an unexpected HTTP error: {}, rethrowing.", e.getStatusCode());
            log.warn("Error Body: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private String getErrorMessage(HttpClientErrorException e) {
        try {
            return mapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (JsonProcessingException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        try {
            String url = this.recommendationServiceUrl + productId;
            log.debug("Calling getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();

            // TODO handle null
            log.debug("Found {} recommendations for product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (RuntimeException e) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}",
                    e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = this.reviewServiceUrl + productId;
            log.debug("Calling getReviews API on URL: {}", url);
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
}
