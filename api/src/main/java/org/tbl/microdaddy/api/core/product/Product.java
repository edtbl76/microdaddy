package org.tbl.microdaddy.api.core.product;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class Product {

    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;

}
