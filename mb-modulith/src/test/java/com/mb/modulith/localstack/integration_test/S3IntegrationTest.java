package com.mb.modulith.localstack.integration_test;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class S3IntegrationTest {

    @Container
    private static final FlociContainer floci = new FlociContainer();

    private S3Client s3;

    @BeforeEach
    void setUp() {
        s3 = S3Client.builder()
                .endpointOverride(URI.create(floci.getEndpoint()))
                .region(Region.of(floci.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
                .forcePathStyle(true)
                .build();
    }

    @Test
    void createBucket_ShouldSuccessfullyCreateBucket_WhenBucketNameIsValid() {
        // Arrange
        String bucketName = "valid-bucket-name";

        // Act
        s3.createBucket(b -> b.bucket(bucketName));

        // Assertions
        assertThat(s3.listBuckets().buckets())
                .anyMatch(b -> b.name().equals(bucketName));
    }

    @Test
    void putObject_ShouldUploadFileSuccessfully_WhenBucketExists() {
        // Arrange
        String bucketName = "upload-bucket";
        String objectKey = "test-file.txt";
        String content = "Hello Floci!";
        s3.createBucket(b -> b.bucket(bucketName));

        // Act
        s3.putObject(p -> p.bucket(bucketName).key(objectKey), RequestBody.fromString(content));

        // Assertions
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        assertThat(s3.headObject(headRequest)).isNotNull();
        assertThat(s3.getObjectAsBytes(b -> b.bucket(bucketName).key(objectKey)).asUtf8String())
                .isEqualTo(content);
    }

    @Test
    void deleteObject_ShouldRemoveFileFromBucket_WhenObjectExists() {
        // Arrange
        String bucketName = "delete-bucket";
        String objectKey = "to-be-deleted.txt";
        s3.createBucket(b -> b.bucket(bucketName));
        s3.putObject(p -> p.bucket(bucketName).key(objectKey), RequestBody.fromString("content"));

        // Act
        s3.deleteObject(d -> d.bucket(bucketName).key(objectKey));

        // Assertions
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        assertThatThrownBy(() -> s3.headObject(headRequest))
                .isInstanceOf(NoSuchKeyException.class);
    }
}
