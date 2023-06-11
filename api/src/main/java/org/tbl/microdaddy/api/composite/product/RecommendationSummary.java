package org.tbl.microdaddy.api.composite.product;

public record RecommendationSummary(
        int recommendationId,
        String author,
        int rate
) { }
