package org.tbl.microdaddy.composite.product;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.tbl.microdaddy.composite.product.services.ProductCompositeIntegration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan("org.tbl")
public class ProductCompositeServiceApplication {

    @Value("${api.common.version}")
    String apiVersion;
    @Value("${api.common.title}")
    String apiTitle;
    @Value("${api.common.description}")
    String apiDescription;
    @Value("${api.common.termsOfService}")
    String apiTermsOfService;
    @Value("${api.common.license}")
    String apiLicense;
    @Value("${api.common.licenseUrl}")
    String apiLicenseUrl;
    @Value("${api.common.externalDocDesc}")
    String apiExternalDocDesc;
    @Value("${api.common.externalDocUrl}")
    String apiExternalDocUrl;
    @Value("${api.common.contact.name}")
    String apiContactName;
    @Value("${api.common.contact.url}")
    String apiContactUrl;
    @Value("${api.common.contact.email}")
    String apiContactEmail;

    @Bean
    public OpenAPI getOpenAPIDocumentation() {
        Contact contact = new Contact()
                .name(apiContactName)
                .url(apiContactUrl)
                .email(apiContactEmail);

        License license = new License()
                .name(apiLicense)
                .url(apiLicenseUrl);

        Info apiInfo = new Info()
                .title(apiTitle)
                .description(apiDescription)
                .contact(contact)
                .termsOfService(apiTermsOfService)
                .license(license);

        ExternalDocumentation externalDocumentation = new ExternalDocumentation()
                .description(apiExternalDocDesc)
                .url(apiExternalDocUrl);

        return new OpenAPI()
                .info(apiInfo)
                .externalDocs(externalDocumentation);
    }

    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    @Autowired
    public ProductCompositeServiceApplication(
            @Value("${app.threadPoolSize:10}")
            Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}")
            Integer taskQueueSize) {

        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    public Scheduler publishEventScheduler() {
        log.info("Creating messageScheduler with connectionPoolSize = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
    }

    @Autowired
    ProductCompositeIntegration integration;

    @Bean
    ReactiveHealthContributor coreServices() {

        final Map<String, ReactiveHealthIndicator> healthIndicatorRegistry = new LinkedHashMap<>();

        healthIndicatorRegistry.put("product", integration::getProductHealth);
        healthIndicatorRegistry.put("recommendation", integration::getRecommendationHealth);
        healthIndicatorRegistry.put("review", integration::getReviewHealth);

        return CompositeReactiveHealthContributor.fromMap(healthIndicatorRegistry);
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }
}
