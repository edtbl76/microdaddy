package org.tbl.microdaddy.composite.product;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
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

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }
}
