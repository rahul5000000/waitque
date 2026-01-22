package com.rrsgroup.common.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder().build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder().build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    public SnsAsyncClient snsAsyncClient() {
        return SnsAsyncClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
