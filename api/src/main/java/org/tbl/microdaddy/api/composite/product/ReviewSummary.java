package org.tbl.microdaddy.api.composite.product;

public record ReviewSummary(
    int reviewId,
    String author,
    String subject
) { }
