package org.tbl.microdaddy.api.core.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductService {

    /**
     * Sample usage:
     *      curl -X POST ${HOST}:${PORT}/product -H "Content-Type: application/json" \
     *      --data `{"productId":1234, "name", "myProduct", "weight":10}`
     * @param body a JSON representation of the new composite
     * @return JSON representation of new product
     */
    @PostMapping(
            value = "/product",
            consumes = "application/json",
            produces = "application/json"
    )
    Product createProduct(@RequestBody Product body);

    /**
     * Usage: "curl ${HOST}:${PORT}/product/1
     * @param productId id of product
     * @return product data, else null
     */
    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Product getProduct(@PathVariable int productId);

    /**
     * Usage: "curl -X DELETE ${HOST}:${PORT}/product/1
     * @param productId id of product
     */
    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);

}
