package org.tbl.microdaddy.core.review;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.core.review.persistence.ReviewEntity;
import org.tbl.microdaddy.core.review.services.ReviewMapper;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTests {

    private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {

        assertNotNull(mapper);

        Review api = new Review(
                1,
                2,
                "author",
                "subject",
                "content",
                "serviceAddress"
        );

        ReviewEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getSubject(), entity.getSubject());
        assertEquals(api.getContent(), entity.getContent());

        Review apiFromMapping = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), apiFromMapping.getProductId());
        assertEquals(api.getReviewId(), apiFromMapping.getReviewId());
        assertEquals(api.getAuthor(), apiFromMapping.getAuthor());
        assertEquals(api.getSubject(), apiFromMapping.getSubject());
        assertEquals(api.getContent(), apiFromMapping.getContent());
        assertNull(apiFromMapping.getServiceAddress());

    }

    @Test
    void mapperListTests() {

        assertNotNull(mapper);

        Review api = new Review(
                1,
                2,
                "author",
                "subject",
                "content",
                "serviceAddress"
        );

        List<Review> reviews = singletonList(api);

        List<ReviewEntity> entities = mapper.apiListToEntityList(reviews);
        assertEquals(reviews.size(), entities.size());

        ReviewEntity entity = entities.get(0);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getSubject(), entity.getSubject());
        assertEquals(api.getContent(), entity.getContent());

        List<Review> reviewsFromEntities = mapper.entityListToApiList(entities);
        assertEquals(reviews.size(), reviewsFromEntities.size());

        Review apiFromMapping = reviewsFromEntities.get(0);

        assertEquals(api.getProductId(), apiFromMapping.getProductId());
        assertEquals(api.getReviewId(), apiFromMapping.getReviewId());
        assertEquals(api.getAuthor(), apiFromMapping.getAuthor());
        assertEquals(api.getSubject(), apiFromMapping.getSubject());
        assertEquals(api.getContent(), apiFromMapping.getContent());
        assertNull(apiFromMapping.getServiceAddress());

    }
}
