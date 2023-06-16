package org.tbl.microdaddy.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.tbl.microdaddy.core.product.persistence.ProductEntity;
import org.tbl.microdaddy.core.product.persistence.ProductRepository;
import org.testcontainers.shaded.org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "name", 1);
        savedEntity = repository.save(entity);

        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {

        ProductEntity entity = new ProductEntity(2, "name", 2);
        repository.save(entity);

        ProductEntity fetchedEntity = repository.findById(entity.getId()).get();
        assertEqualsProduct(entity, fetchedEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setName("name2");
        repository.save(savedEntity);

        ProductEntity fetchedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) fetchedEntity.getVersion());
        assertEquals("name2", fetchedEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }


    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    void validateDuplicateKeyException() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "name", 1);
        assertThrows(DuplicateKeyException.class, () -> repository.save(entity));
    }

    @Test
    void validateOptimisticLockingFailureException() {

        // Store saved setup entity into 2 separate entity objects
        ProductEntity entityOne = repository.findById(savedEntity.getId()).get();
        ProductEntity entityTwo = repository.findById(savedEntity.getId()).get();

        // Update entity using the first entity object (which will increment the version number)
        entityOne.setName("nameUpdatedFromEntityOne");
        repository.save(entityOne);

        // update entity using second entity, which fails because 2nd entity has original version.
        entityTwo.setName("nameUpdatedFromEntityTwo");
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entityTwo));

    }

    @Test
    void validatePaging() {

        repository.deleteAll();

        List<ProductEntity> products = rangeClosed(100, 110)
                .mapToObj(id -> new ProductEntity(id, "name " + id, id))
                .collect(Collectors.toList());
        repository.saveAll(products);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
        nextPage = testNextPage(nextPage, "[101, 102, 103, 104]", true);
        nextPage = testNextPage(nextPage, "[105, 106, 107, 108]", true);
        nextPage = testNextPage(nextPage, "[109, 110]", false);
    }

    // Helpers
    private Pageable testNextPage(Pageable page, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(page);

        String pageMap = productPage.getContent().stream()
                .map(ProductEntity::getProductId)
                .toList()
                .toString();

        assertEquals(expectedProductIds, pageMap);
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }


    private void assertEqualsProduct(ProductEntity expected, ProductEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getProductId(), actual.getProductId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getWeight(), actual.getWeight());
    }
}
