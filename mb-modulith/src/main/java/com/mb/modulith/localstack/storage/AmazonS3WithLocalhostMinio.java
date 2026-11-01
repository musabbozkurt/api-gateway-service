package com.mb.modulith.localstack.storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mb.modulith.localstack.config.StaticAwsCredentials;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static com.amazonaws.SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY;

@Slf4j
public class AmazonS3WithLocalhostMinio {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String KEY_NAME = "passwd";
    private static final String UPLOAD_FILE_NAME = "/etc/passwd";
    private static final String SERVICE_ENDPOINT = "http://127.0.0.1:9000";
    // Local MinIO development credentials, overridable with environment variables (same as application.yml: minio.*).
    private static final String ACCESS_KEY = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "admin");
    private static final String SECRET_KEY = System.getenv().getOrDefault("MINIO_SECRET", "supersecret");

    static void main() {
        System.setProperty(DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");

        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(SERVICE_ENDPOINT, Regions.US_EAST_1.getName()))
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(new StaticAwsCredentials(ACCESS_KEY, SECRET_KEY)))
                .build();

        // AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
        try {
            log.info("Uploading a new object to S3 from a file\n");
            boolean b = s3Client.doesBucketExistV2(BUCKET_NAME);
            if (!b) {
                s3Client.createBucket(BUCKET_NAME);
            }

            File file = new File(UPLOAD_FILE_NAME);

            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, KEY_NAME, file));

            URL url = s3Client.generatePresignedUrl(BUCKET_NAME, KEY_NAME, Date.from(ZonedDateTime.now().toInstant().atZone(ZoneId.of("GMT+3")).plusMinutes(2).toInstant()));

            log.info(url.toString());
        } catch (AmazonServiceException ase) {
            log.info("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
            log.info("Error Message AmazonServiceException: {}", ase.getMessage());
            log.info("HTTP Status Code: {}", ase.getStatusCode());
            log.info("AWS Error Code:   {}", ase.getErrorCode());
            log.info("Error Type:       {}", ase.getErrorType());
            log.info("Request ID:       {}", ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.info("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
            log.info("Error Message AmazonClientException: {}", ace.getMessage());
        }
    }
}
