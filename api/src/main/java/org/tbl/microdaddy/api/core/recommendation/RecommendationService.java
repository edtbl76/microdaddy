package org.tbl.microdaddy.api.core.recommendation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecommendationService {


    /**
     * Sample usage:
     *      curl -X POST ${HOST}:${PORT}/recommendation -H "Content-Type: application/json" \
     *      --data `{"productId":1234, "recommendationId": 5678, "author": "me", "rate": 0, "content": "BoooOoo!" }'
     *
     * @param body a JSON representation of the new composite
     * @return JSON representation of new product
     */
    @PostMapping(
            value = "/recommendation",
            consumes = "application/json",
            produces = "application/json"
    )
    Recommendation createRecommendation(@RequestBody Recommendation body);

    /**
     * Usage: "curl ${HOST}:${PORT}/recommendation?productId=1
     *
     * @param productId id of product
     * @return recommendations of requested product, else null
     */
    @GetMapping(
            value = "/recommendation",
            produces = "application/json")
    List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);


    /**
     * Usage: "curl -X DELETE ${HOST}:${PORT}/recommendation?productId=1
     *
     * @param productId id of product
     */
    @DeleteMapping(value = "/recommendation")
    void deleteRecommendation(@RequestParam(value = "productId") int productId);
}
