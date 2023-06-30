package org.tbl.microdaddy.composite.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/openapi/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(POST, "/product-composite/**").hasAuthority("SCOPE_product:write")
                        .pathMatchers(DELETE, "/product-composite/**").hasAuthority("SCOPE_product:write")
                        .pathMatchers(GET, "/product-composite/**").hasAuthority("SCOPE_product:read")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(withDefaults()));
        return http.build();
    }
}
