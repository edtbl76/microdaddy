package org.tbl.microdaddy.core.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.tbl.microdaddy.core.review.persistence.ReviewEntity;
import org.tbl.microdaddy.core.review.persistence.ReviewRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = NONE)
class PersistenceTests extends MySqlTestBase {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ReviewEntity entity = new ReviewEntity(
                1, 2, "author", "subject", "content");

        savedEntity = repository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }

    @Test
    void create() {

        ReviewEntity createdEntity = new ReviewEntity(
                1, 3, "author", "subject", "content");

        repository.save(createdEntity);

        // TODO: This is a test, so not sure I care about the Optional.
        ReviewEntity fetchedEntity = repository.findById(createdEntity.getId()).get();
        assertEqualsReview(createdEntity, fetchedEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {

        savedEntity.setAuthor("authorTwo");
        repository.save(savedEntity);

        // TODO: see above re: get()
        ReviewEntity fetchedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) fetchedEntity.getVersion());
        assertEquals("authorTwo", fetchedEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<ReviewEntity> entities = repository.findByProductId(savedEntity.getProductId());

        assertThat(entities, hasSize(1));
        assertEqualsReview(savedEntity, entities.get(0));
    }


    @Test
    void validateDuplicationFailure() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            ReviewEntity duplicateEntity  = new ReviewEntity(
                    1, 2, "author", "subject", "content");
            repository.save(duplicateEntity);
        });
    }

    @Test
    void validateOptimisticLockingFailure() {

        // store same entity in 2 separate objects
        ReviewEntity entityOne = repository.findById(savedEntity.getId()).get();
        ReviewEntity entityTwo = repository.findById(savedEntity.getId()).get();

        // Update entity via first object
        entityOne.setAuthor("authorThree");
        repository.save(entityOne);

        /*
            Ensure that we aren't allowed to update the entity w/ the second object,
            because it holds an old version number.

            (NOTE: This pattern is important. It is the action of persisting the entity
            that will cause this exception as opposed to updating the entity.)
         */
        entityTwo.setAuthor("authorFour");
        assertThrows(OptimisticLockingFailureException.class, () -> {
            repository.save(entityTwo);
        });
    }

    private void assertEqualsReview(ReviewEntity expected, ReviewEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getProductId(), actual.getProductId());
        assertEquals(expected.getReviewId(), actual.getReviewId());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getSubject(), actual.getSubject());
        assertEquals(expected.getContent(), actual.getContent());
    }
}
