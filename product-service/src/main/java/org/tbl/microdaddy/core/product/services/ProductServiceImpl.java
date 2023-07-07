package org.tbl.microdaddy.core.product.services;

import io.micrometer.observation.annotation.Observed;
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


import java.time.Duration;
import java.util.Random;

import static java.time.Duration.ofSeconds;
import static java.util.logging.Level.FINE;

@RestController
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final String INVALID_PRODUCT_ID = "Invalid productId: ";


    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }



    @Observed(
            name = "createProduct",
            contextualName = "product-service.create-product"
    )
    @Override
    public Mono<Product> createProduct(Product body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException(INVALID_PRODUCT_ID + body.getProductId());
        }

        ProductEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
               .log(log.getName(), FINE)
               .onErrorMap(
                       DuplicateKeyException.class,
                       ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
               .map(mapper::entityToApi);

    }

    @Observed(
            name = "getProduct",
            contextualName = "product-service.get-product"
    )
    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {

        if (productId < 1) {
            throw new InvalidInputException(INVALID_PRODUCT_ID+ productId);
        }

        log.info("calling getProduct for productId={}", productId);

        return repository.findByProductId(productId)
                // Chaos Code
                .map(entity -> throwErrorIfCircuitBreakerFault(entity, faultPercent))
                .delayElement(ofSeconds(delay))
                // Normal Code
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(log.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Observed(
            name = "deleteProduct",
            contextualName = "product-service.delete-product"
    )
    @Override
    public Mono<Void> deleteProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException(INVALID_PRODUCT_ID + productId);
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

    private ProductEntity throwErrorIfCircuitBreakerFault(ProductEntity entity, int faultPercent) {
        if (faultPercent == 0) {
            return entity;
        }

        int randomThreshold = getRandomNumber(1, 100);

        if (faultPercent < randomThreshold) {
            log.debug("No error occurred: {} < {}", faultPercent, randomThreshold);
        } else {
            log.debug("Circuit Breaker Fault: Error Occurred: {} >= {}", faultPercent, randomThreshold);
            throw new RuntimeException("Circuit Breaker Fault!");
        }

        return entity;
    }

    private final Random randomNumberGenerator = new Random();

    private int getRandomNumber(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        return randomNumberGenerator.nextInt((max - min) + 1) + min;
    }
}
