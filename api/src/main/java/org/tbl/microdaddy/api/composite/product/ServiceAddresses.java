package org.tbl.microdaddy.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ServiceAddresses {

    private final String compositeAddress;
    private final String productAddress;
    private final String reviewAddress;
    private final String recommendationAddress;

    public ServiceAddresses() {
        this.compositeAddress = null;
        this.productAddress = null;
        this.reviewAddress = null;
        this.recommendationAddress = null;
    }

}
