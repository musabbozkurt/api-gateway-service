package com.mb.modulith.localstack.support;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * LocalStack container that only exposes DynamoDB and offers the same helper methods as the former
 * {@code org.testcontainers:dynalite} module ({@code DynaliteContainer}).
 * That module does not exist in Testcontainers 2.x and the {@code quay.io/testcontainers/dynalite} image uses a Docker
 * schema v1 manifest that current Docker engines (containerd 2.1+) refuse to pull.
 */
public class DynamoDbLocalStackContainer extends LocalStackContainer {

    @SuppressWarnings("resource") // lifecycle is managed by Testcontainers (@Container) or the JVM Ryuk reaper
    public DynamoDbLocalStackContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withServices(Service.DYNAMODB.getLocalStackName());
        String authToken = System.getenv("LOCALSTACK_AUTH_TOKEN");
        withEnv("LOCALSTACK_AUTH_TOKEN", StringUtils.isBlank(authToken) ? "test-token" : authToken);
    }

    public AwsClientBuilder.EndpointConfiguration getEndpointConfiguration() {
        return new AwsClientBuilder.EndpointConfiguration(getEndpoint().toString(), getRegion());
    }

    public AWSStaticCredentialsProvider getCredentials() {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(getAccessKey(), getSecretKey()));
    }

    public AmazonDynamoDB getClient() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(getEndpointConfiguration())
                .withCredentials(getCredentials())
                .build();
    }
}
