package com.mb.modulith.localstack.config;

import com.amazonaws.auth.AWSCredentials;
import org.jspecify.annotations.NonNull;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

/**
 * Access key / secret pair for the local development endpoints (LocalStack and MinIO).
 * <p>
 * The values are never hard-coded: they are injected from configuration ({@code amazon.aws.*}, {@code minio.*}) or
 * environment variables. The same value object serves both the AWS SDK v1 ({@link AWSCredentials}) and the
 * AWS SDK v2 ({@link AwsCredentials}) clients.
 */
public record StaticAwsCredentials(String accessKey, String secretKey) implements AWSCredentials, AwsCredentials {

    @Override
    public String getAWSAccessKeyId() {
        return accessKey;
    }

    @Override
    public String getAWSSecretKey() {
        return secretKey;
    }

    @Override
    public String accessKeyId() {
        return accessKey;
    }

    @Override
    public String secretAccessKey() {
        return secretKey;
    }

    @Override
    public @NonNull String toString() {
        return "StaticAwsCredentials[accessKey=" + accessKey + ", secretKey=****]";
    }
}
