package org.tbl.microdaddy.core.product.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.product.ProductService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.api.exceptions.NotFoundException;
import org.tbl.microdaddy.core.product.persistence.ProductEntity;
import org.tbl.microdaddy.core.product.persistence.ProductRepository;
import org.tbl.microdaddy.util.http.ServiceUtil;
import reactor.core.publisher.Mono;


import static java.util.logging.Level.FINE;

@RestController
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Mono<Product> createProduct(Product body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }

        ProductEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
               .log(log.getName(), FINE)
               .onErrorMap(
                       DuplicateKeyException.class,
                       ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
               .map(mapper::entityToApi);

    }

    @Override
    public Mono<Product> getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.info("calling getProduct for productId={}", productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(log.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteProduct: attempts to delete an entity with productId: {}", productId);

        return repository.findByProductId(productId)
                .log(log.getName(), FINE)
                .map(repository::delete)
                .flatMap(voidMono -> voidMono);
    }

    private Product setServiceAddress(Product product) {
        product.setServiceAddress(serviceUtil.getServiceAddress());
        return product;
    }
}
