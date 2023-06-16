package org.tbl.microdaddy.core.recommendation;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@TestPropertySource(properties = "spring.autoconfigure.exclude=" +
        "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration")
public abstract class MongoDbTestBase {

    private static MongoDBContainer database = new MongoDBContainer("mongo:6.0.6");

    static {
        database.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", database::getHost);
        registry.add("spring.data.mongodb.port", database::getFirstMappedPort);
        registry.add("spring.data.mongodb.database", () -> "test");
    }
}
