package org.tbl.microdaddy.core.product;

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.tbl.microdaddy.core.product.persistence.ProductEntity;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class})
@ComponentScan("org.tbl.microdaddy")
public class ProductServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ProductServiceApplication.class, args);

		String mongoDbHost = context.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongoDbPort = context.getEnvironment().getProperty("spring.data.mongodb.port");
		String mongoDBAuthSource = context.getEnvironment().getProperty("spring.data.mongodb.authentication-database");
		log.info("Connected to MongoDB: " + mongoDbHost + ":" + mongoDbPort + ", " + mongoDBAuthSource);

	}
	@Autowired
	ReactiveMongoOperations mongoTemplate;

	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext =
				mongoTemplate.getConverter().getMappingContext();

		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

		ReactiveIndexOperations indexOperations = mongoTemplate.indexOps(ProductEntity.class);
		resolver.resolveIndexFor(ProductEntity.class).forEach(indexOperations::ensureIndex);
	}

}
