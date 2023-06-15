package org.tbl.microdaddy.api.core.review;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ReviewService {


    /**
     * Sample usage:
     *      curl -X POST ${HOST}:${PORT}/review -H "Content-Type: application/json" \
     *      --data `{"productId":1234, "reviewId": 5678, "author": "me", "subject": "Cool!", "content": "Neat Product" }'
     *
     * @param body a JSON representation of the new composite
     * @return JSON representation of new product
     */
    @PostMapping(
            value = "/review",
            consumes = "application/json",
            produces = "applicaiton/json"
    )
    Review createReview(@RequestBody Review body);

    /**
     * Usage: "curl ${HOST}:${PORT}/review?productId=1
     *
     * @param productId id of product
     * @return reviews of requested product, else null
     */
    @GetMapping(
            value = "/review",
            produces = "application/json")
    List<Review> getReviews(@RequestParam(value = "productId") int productId);

    /**
     * Usage: "curl -X DELETE ${HOST}:${PORT}/review?productId=1
     *
     * @param productId id of product
     */
    @DeleteMapping(value = "/review")
    void deleteReview(@RequestParam(value = "productId", required = true) int productId);
}
