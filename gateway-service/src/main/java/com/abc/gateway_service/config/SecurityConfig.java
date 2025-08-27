package com.abc.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // Stateless
                .authorizeExchange(exchanges -> exchanges
                        // Cho phép health, docs
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Cho phép auth
                        .pathMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()

                        // ✅ Cho phép verify qua users-service
                        .pathMatchers(HttpMethod.GET, "/users/verify").permitAll()
                        .pathMatchers(HttpMethod.POST, "/users/verify").permitAll()

                        // Các request còn lại cần xác thực
                        .anyExchange().authenticated()
                )
                .build();
    }
}
