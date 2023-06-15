package org.tbl.microdaddy.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class Review {

    private int productId;
    private int reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;

}
