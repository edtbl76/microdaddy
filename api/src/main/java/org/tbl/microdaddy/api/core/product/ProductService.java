package org.tbl.microdaddy.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> createProduct(Product body);

    /**
     * Usage: "curl ${HOST}:${PORT}/product/1
     * @param productId id of product
     * @return product data, else null
     */
    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productId);

    Mono<Void> deleteProduct(int productId);

}
