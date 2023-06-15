package org.tbl.microdaddy.api.core.product;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Product {

    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;

}
