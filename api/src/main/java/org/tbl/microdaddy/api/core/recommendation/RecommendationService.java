package org.tbl.microdaddy.api.core.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {

    Mono<Recommendation> createRecommendation(Recommendation body);

    /**
     * Usage: "curl ${HOST}:${PORT}/recommendation?productId=1
     *
     * @param productId id of product
     * @return recommendations of requested product, else null
     */
    @GetMapping(
            value = "/recommendation",
            produces = "application/json")
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);

    Mono<Void> deleteRecommendations(int productId);
}
