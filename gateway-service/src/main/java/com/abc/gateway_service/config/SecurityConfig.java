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

                        // Allow auth endpoints with or without an `/api` prefix
                        .pathMatchers(HttpMethod.POST,
                                "/auth/register", "/auth/login",
                                "/api/auth/register", "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/auth/verify", "/api/auth/verify").permitAll()
                        .pathMatchers(HttpMethod.POST,
                                "/auth/verify", "/api/auth/verify").permitAll()

                        // ✅ Allow user-service verify endpoints (also support `/api` prefix)
                        .pathMatchers(HttpMethod.GET,
                                "/users/verify", "/api/users/verify").permitAll()
                        .pathMatchers(HttpMethod.POST,
                                "/users/verify", "/api/users/verify").permitAll()

                        // Các request còn lại cần xác thực
                        .anyExchange().authenticated()
                )
                .build();
    }
}
