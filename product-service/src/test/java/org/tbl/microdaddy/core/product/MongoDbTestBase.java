package org.tbl.microdaddy.core.product;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public abstract class MongoDbTestBase {

    private static MongoDBContainer database = new MongoDBContainer("mongo:6.3.1");

    static {
        database.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongo.host", database::getHost);
        registry.add("spring.data.mongo.port", () -> database.getMappedPort(27017));
        registry.add("spring.data.mongo.database", () -> "test");
    }
}
