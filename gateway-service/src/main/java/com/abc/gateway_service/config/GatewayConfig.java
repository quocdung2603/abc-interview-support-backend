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
                    // Forward Authorization header as-is; in future decode JWT and add claims
                    HttpHeaders headers = exchange.getRequest().getHeaders();
                    String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
                    ServerWebExchange mutated = exchange.mutate()
                            .request(builder -> {
                                if (auth != null) {
                                    builder.header("X-User-Authorization", auth);
                                }
                            })
                            .build();
                    return chain.filter(mutated);
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
