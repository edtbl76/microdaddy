package org.tbl.microdaddy.authz.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

@Slf4j
@EnableWebSecurity
@Configuration
public class DefaultSecurityConfiguration {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ROLE = "USER";

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated())
                .formLogin(withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService users() {
        UserDetails userDetails = User.builder()
                .username(USERNAME)
                .password(passwordEncoder().encode(PASSWORD))
                .roles(ROLE)
                .build();

        log.info(userDetails.getPassword());
        log.info(userDetails.getPassword());
        log.info(userDetails.getPassword());
        log.info(userDetails.getPassword());
        log.info(userDetails.getPassword());

       return new InMemoryUserDetailsManager(userDetails);

    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return createDelegatingPasswordEncoder();
    }

}
