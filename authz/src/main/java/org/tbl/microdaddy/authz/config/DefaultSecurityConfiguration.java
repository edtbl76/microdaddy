package org.tbl.microdaddy.authz.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@EnableWebSecurity
public class DefaultSecurityConfiguration {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ROLE = "USER";

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated())
                .formLogin(withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService users() {
       return new InMemoryUserDetailsManager(User.builder()
                .username(USERNAME)
                .password(new BCryptPasswordEncoder().encode(PASSWORD))
                .roles(ROLE)
                .build());

    }
}
