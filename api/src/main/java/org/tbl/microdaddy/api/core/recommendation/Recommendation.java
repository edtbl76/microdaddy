package org.tbl.microdaddy.api.core.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recommendation{

    private int productId;
    private int recommendationId;
    private String author;
    private int rate;
    private String content;
    private String serviceAddress;

    public Recommendation() {
        this.productId = 0;
        this.recommendationId = 0;
        this.author = null;
        this.rate = 0;
        this.content = null;
        this.serviceAddress = null;
    }

}
