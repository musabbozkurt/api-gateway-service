package com.mb.modulith.localstack.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("minio")
public class MinioCloudConfiguration {

    private String endpoint;
    private String publicUrl;
    private String accessKey;
    private String secret;
    private String bucket;
    private String microservicePrefix;
    private String filePrefix;

    @Bean
    public AmazonS3 amazonS3() {
        return getAmazonS3(this.endpoint);
    }

    @Bean
    public AmazonS3 amazonS3ForSignedUrl() {
        return getAmazonS3(this.publicUrl);
    }

    private AmazonS3 getAmazonS3(String url) {
        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, Regions.US_EAST_1.getName()))
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(new StaticAwsCredentials(this.accessKey, this.secret)))
                .build();
    }
}
