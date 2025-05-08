package com.epsilon.auto.poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DemoS3Config {
    @Bean
    RestClient restClient() {
        return RestClient.create();
    }
}
