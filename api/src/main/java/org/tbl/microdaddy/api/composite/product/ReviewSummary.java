package org.tbl.microdaddy.api.composite.product;

public record ReviewSummary(
    int reviewId,
    String author,
    String subject,
    String content
) {

    public ReviewSummary() {
        this(0, null, null, null);
    }
}
