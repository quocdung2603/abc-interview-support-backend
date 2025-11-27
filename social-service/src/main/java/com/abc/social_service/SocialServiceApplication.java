package com.abc.social_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SocialServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }
}
