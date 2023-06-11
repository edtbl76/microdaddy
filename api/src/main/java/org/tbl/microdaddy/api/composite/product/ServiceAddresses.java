package org.tbl.microdaddy.api.composite.product;

public record ServiceAddresses(
    String compositeAddress,
    String productAddress,
    String reviewAddress,
    String recommendationAddress
) {

    public ServiceAddresses() {
        this(null, null, null, null);
    }
}
