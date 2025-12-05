package com.abc.gateway_service.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class GatewayConfig {

    // Equivalent of AddUserInfoToHeader filter name used in config-repo
    @Bean(name = "AddUserInfoToHeader")
    public AbstractGatewayFilterFactory<Object> addUserInfoToHeader() {
        return new AbstractGatewayFilterFactory<>() {
            @Override
            public GatewayFilter apply(Object config) {
                return (exchange, chain) -> {
                    // Extract user ID from JWT token and add as header
                    HttpHeaders headers = exchange.getRequest().getHeaders();
                    String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
                    
                    ServerWebExchange.Builder builder = exchange.mutate().request(requestBuilder -> {
                        if (auth != null) {
                            requestBuilder.header("X-User-Authorization", auth);
                            
                            // Extract user ID from JWT token
                            try {
                                if (auth.startsWith("Bearer ")) {
                                    String token = auth.substring(7);
                                    // Decode JWT payload (base64)
                                    String[] parts = token.split("\\.");
                                    if (parts.length >= 2) {
                                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                                        // Extract "sub" claim (user ID)
                                        if (payload.contains("\"sub\"")) {
                                            String sub = payload.split("\"sub\":\"")[1].split("\"")[0];
                                            requestBuilder.header("X-User-Id", sub);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // If extraction fails, continue without X-User-Id header
                                System.err.println("Failed to extract user ID from token: " + e.getMessage());
                            }
                        }
                    });
                    
                    return chain.filter(builder.build());
                };
            }

            @Override
            public List<String> shortcutFieldOrder() {
                return List.of();
            }
        };
    }

    // Key resolver bean name to match "@remoteAddrKeyResolver" in config-repo
    @Bean(name = "remoteAddrKeyResolver")
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver remoteAddrKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown");
    }
}
