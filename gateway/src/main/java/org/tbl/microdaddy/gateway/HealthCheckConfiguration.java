package org.tbl.microdaddy.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.logging.Level.FINE;

@Slf4j
@Configuration
public class HealthCheckConfiguration {

    private WebClient webClient;

    @Autowired
    public HealthCheckConfiguration(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Bean
    ReactiveHealthContributor healthcheckMicroservices() {
        final Map<String, ReactiveHealthIndicator> healthCheckRegistry = new LinkedHashMap<>();

        healthCheckRegistry.put("product", () -> getHealth("http://product"));
        healthCheckRegistry.put("recommendation", () -> getHealth("http://recommendation"));
        healthCheckRegistry.put("review", () -> getHealth("http://review"));
        healthCheckRegistry.put("product-composite", () -> getHealth("http://product-composite"));
        healthCheckRegistry.put("auth-server", () -> getHealth("http://auth-server"));

        return CompositeReactiveHealthContributor.fromMap(healthCheckRegistry);
    }

    private Mono<Health> getHealth(String baseUrl) {
        String url = baseUrl + "/actuator/health";
        log.debug("Calling health API at URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(str -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(log.getName(), FINE);
    }
}
