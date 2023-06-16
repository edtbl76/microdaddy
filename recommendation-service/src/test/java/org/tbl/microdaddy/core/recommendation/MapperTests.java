package org.tbl.microdaddy.core.recommendation;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.core.recommendation.persistence.RecommendationEntity;
import org.tbl.microdaddy.core.recommendation.services.RecommendationMapper;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTests {

    private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Recommendation api = new Recommendation(
                1,
                2,
                "author",
                4,
                "content",
                "serviceAddress");
        RecommendationEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());

        Recommendation apiMappedFromEntity = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), apiMappedFromEntity.getProductId());
        assertEquals(api.getRecommendationId(), apiMappedFromEntity.getRecommendationId());
        assertEquals(api.getAuthor(), apiMappedFromEntity.getAuthor());
        assertEquals(api.getRate(), apiMappedFromEntity.getRate());
        assertEquals(api.getContent(), apiMappedFromEntity.getContent());
        assertNull(apiMappedFromEntity.getServiceAddress());

    }

    @Test
    void mapperListTests() {

        assertNotNull(mapper);

        Recommendation api = new Recommendation(
                1,
                2,
                "author",
                4,
                "content",
                "serviceAddress");
        List<Recommendation> apiList = singletonList(api);

        List<RecommendationEntity> entities = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entities.size());

        RecommendationEntity entity = entities.get(0);
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());

        List<Recommendation> apiListTwo = mapper.entityListToApiList(entities);
        assertEquals(apiList.size(), apiListTwo.size());


        Recommendation apiTwo = apiListTwo.get(0);
        assertEquals(api.getProductId(), apiTwo.getProductId());
        assertEquals(api.getRecommendationId(), apiTwo.getRecommendationId());
        assertEquals(api.getAuthor(), apiTwo.getAuthor());
        assertEquals(api.getRate(), apiTwo.getRate());
        assertEquals(api.getContent(), apiTwo.getContent());
        assertNull(apiTwo.getServiceAddress());

    }
}
