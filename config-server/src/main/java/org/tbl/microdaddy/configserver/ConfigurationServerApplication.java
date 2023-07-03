package org.tbl.microdaddy.configserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@EnableConfigServer
@SpringBootApplication
public class ConfigurationServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ConfigurationServerApplication.class, args);

        String searchLocations = context
                .getEnvironment()
                .getProperty("spring.cloud.config.server.native.search-locations");

        log.info("Serving configurations from native location: " + searchLocations);
    }
}
