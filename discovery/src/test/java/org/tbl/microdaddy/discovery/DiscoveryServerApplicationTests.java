package org.tbl.microdaddy.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.cloud.config.enabled=false"}
)
class DiscoveryServerApplicationTests {

    @Value("${app.eureka-username}")
    private String username;
    @Value("${app.eureka-password}")
    private String password;

    private TestRestTemplate testRestTemplate;

    @Autowired
    void setTestRestTemplate(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate.withBasicAuth(username, password);
    }

    @Test
    void validateLoadCatalog() {

        String expectedResponse =
                "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}";
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/eureka/apps", String.class);
        assertEquals(OK, entity.getStatusCode());
        assertEquals(expectedResponse, entity.getBody());


    }

    @Test
    void validateHealthCheck() {

        String expectedResponse = "{\"status\":\"UP\"}";
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(OK, entity.getStatusCode());
        assertEquals(expectedResponse, entity.getBody());
    }
}
