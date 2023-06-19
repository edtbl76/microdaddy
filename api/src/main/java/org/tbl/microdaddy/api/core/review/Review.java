package org.tbl.microdaddy.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Review {

    private int productId;
    private int reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;

    public Review() {
        this.productId = 0;
        this.reviewId = 0;
        this.author = null;
        this.subject = null;
        this.content = null;
        this.serviceAddress = null;
    }

}
