package org.tbl.microdaddy.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.tbl.microdaddy.core.product.persistence.ProductEntity;
import org.tbl.microdaddy.core.product.persistence.ProductRepository;
import reactor.test.StepVerifier;

import java.util.Objects;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {

        // StepVerifiers are a way to test reactive code by walking through
        // the steps one at a time in a 2-phase approach.. do the thing, wait for result.
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = new ProductEntity(1, "name", 1);

        StepVerifier.create(repository.save(entity))
                        .expectNextMatches(productEntity -> {
                            savedEntity = productEntity;
                            return areProductsEqual(entity, savedEntity);
                        }).verifyComplete();
    }

    @Test
    void create() {

        ProductEntity entity = new ProductEntity(2, "name", 2);
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(productEntity -> entity.getProductId() == productEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.findById(entity.getId()))
                .expectNextMatches(productEntity -> areProductsEqual(entity, productEntity))
                .verifyComplete();


        StepVerifier.create(repository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void update() {
        savedEntity.setName("name2");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(productEntity -> productEntity.getName().equals("name2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(
                        productEntity -> productEntity.getVersion() == 1 && productEntity.getName().equals("name2"))
                .verifyComplete();

    }

    @Test
    void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }


    @Test
    void getByProductId() {

        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(productEntity -> areProductsEqual(savedEntity, productEntity))
                .verifyComplete();

    }

    @Disabled
    @Test
    void validateDuplicateKeyException() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "name", 1);
        StepVerifier.create(repository.save(entity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void validateOptimisticLockingFailureException() {

        // Store saved setup entity into 2 separate entity objects
        ProductEntity entityOne = repository.findById(savedEntity.getId()).block();
        ProductEntity entityTwo = repository.findById(savedEntity.getId()).block();

        // Update entity using the first entity object (which will increment the version number)
        entityOne.setName("nameUpdatedFromEntityOne");
        repository.save(entityOne).block();

        // update entity using second entity, which fails because 2nd entity has original version.
        entityTwo.setName("nameUpdatedFromEntityTwo");
        StepVerifier.create(repository.save(entityTwo))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        // Get updated entity and validate state
        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(productEntity ->
                        productEntity.getVersion() == 1 && productEntity.getName().equals("nameUpdatedFromEntityOne"))
                .verifyComplete();
    }

    private boolean areProductsEqual(ProductEntity expected, ProductEntity actual) {
        return (expected.getId().equals(actual.getId())) &&
                (Objects.equals(expected.getVersion(), actual.getVersion())) &&
                (expected.getProductId() == actual.getProductId()) &&
                (expected.getName().equals(actual.getName())) &&
                (expected.getWeight() == actual.getWeight());

    }
}
