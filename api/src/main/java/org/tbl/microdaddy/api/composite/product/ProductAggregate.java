package org.tbl.microdaddy.api.composite.product;

import java.util.List;

public record ProductAggregate(
        int productId,
        String name,
        int weight,
        List<RecommendationSummary> recommendations,
        List<ReviewSummary> reviews,
        ServiceAddresses serviceAddresses
) {

    public ProductAggregate() {
        this(0, null, 0, null, null, null);
    }
}
