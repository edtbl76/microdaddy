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

        assertEquals(api.productId(), entity.productId());
        assertEquals(api.recommendationId(), entity.recommendationId());
        assertEquals(api.author(), entity.author());
        assertEquals(api.rate(), entity.rating());
        assertEquals(api.content(), entity.content());

        Recommendation apiMappedFromEntity = mapper.entityToApi(entity);

        assertEquals(api.productId(), apiMappedFromEntity.productId());
        assertEquals(api.recommendationId(), apiMappedFromEntity.recommendationId());
        assertEquals(api.author(), apiMappedFromEntity.author());
        assertEquals(api.rate(), apiMappedFromEntity.rate());
        assertEquals(api.content(), apiMappedFromEntity.content());
        assertNull(apiMappedFromEntity.serviceAddress());

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
        assertEquals(api.productId(), entity.productId());
        assertEquals(api.recommendationId(), entity.recommendationId());
        assertEquals(api.author(), entity.author());
        assertEquals(api.rate(), entity.rating());
        assertEquals(api.content(), entity.content());

        List<Recommendation> apiListTwo = mapper.entityListToApiList(entities);
        assertEquals(apiList.size(), apiListTwo.size());


        Recommendation apiTwo = apiListTwo.get(0);
        assertEquals(api.productId(), apiTwo.productId());
        assertEquals(api.recommendationId(), apiTwo.recommendationId());
        assertEquals(api.author(), apiTwo.author());
        assertEquals(api.rate(), apiTwo.rate());
        assertEquals(api.content(), apiTwo.content());
        assertNull(apiTwo.serviceAddress());





    }
}
