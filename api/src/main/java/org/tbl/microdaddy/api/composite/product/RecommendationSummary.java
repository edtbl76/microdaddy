package org.tbl.microdaddy.api.composite.product;

public record RecommendationSummary(
        int recommendationId,
        String author,
        int rate,
        String content
) {

    public RecommendationSummary() {
        this(0, null, 0, null);
    }
}
