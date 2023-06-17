package org.tbl.microdaddy.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

    Mono<Review> createReview(Review body);

    /**
     * Usage: "curl ${HOST}:${PORT}/review?productId=1
     *
     * @param productId id of product
     * @return reviews of requested product, else null
     */
    @GetMapping(
            value = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "productId") int productId);

    Mono<Void> deleteReviews(int productId);
}
