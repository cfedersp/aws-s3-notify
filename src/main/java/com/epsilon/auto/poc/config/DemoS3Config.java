package com.epsilon.auto.poc.config;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.services.s3.S3Client;
@Slf4j
@Configuration
public class DemoS3Config {

    @Value("${aws.profile:#{null}}")
    protected String profile;
    @Value("${input.bucket.region}")
    protected String region;

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public S3Client s3Client() {
        if(StringUtils.isBlank(profile)) {
            log.warn("No AWS profile specified");
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        } else {
            return S3Client.builder()
                    .credentialsProvider(ProfileCredentialsProvider.create(profile))
                    .region(Region.of(region))
                    .build();
        }
    }
}
