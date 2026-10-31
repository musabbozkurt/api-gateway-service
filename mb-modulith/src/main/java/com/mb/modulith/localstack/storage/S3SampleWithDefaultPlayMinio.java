package com.mb.modulith.localstack.storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.AwsHostNameUtils;
import com.mb.modulith.localstack.config.StaticAwsCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/*
 *
 * https://docs.min.io/docs/how-to-use-aws-sdk-for-java-with-minio-server.html
 * https://stackoverflow.com/questions/49332533/using-s3-java-sdk-to-talk-to-s3-compatible-storage-minio
 * https://gist.githubusercontent.com/krishnasrinivas/edc62873bcef5396493f31f8d582fc2c/raw/7d7c6d7279ccebc7b73c18e130bbbce6b05e0590/UploadObjectSingleOperation.java
 * https://thedatabaseme.de/2022/03/20/i-do-it-on-my-own-then-self-hosted-s3-object-storage-with-minio-and-docker/
 * https://github.com/minio/minio-js/issues/588
 * https://stackoverflow.com/questions/70418398/restricting-access-to-s3-objects-unless-url-is-signed
 * https://github.com/minio/minio/issues/4687
 * https://github.com/minio/minio-java/blob/master/examples/SetBucketPolicy.java
 * https://docs.min.io/docs/minio-multi-user-quickstart-guide.html
 * https://docs.min.io/docs/minio-bucket-lifecycle-guide.html
 * https://docs.min.io/minio/baremetal/reference/minio-mc/mc-policy-set.html
 *
 * */

@Slf4j
public class S3SampleWithDefaultPlayMinio {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String KEY_NAME = "hosts";
    private static final String UPLOAD_FILE_NAME = "/etc/hosts";
    private static final String SERVICE_ENDPOINT = "https://play.min.io";
    // Credentials of the MinIO playground (https://play.min.io) are not stored in the code, provide them with
    // the MINIO_PLAY_ACCESS_KEY and MINIO_PLAY_SECRET_KEY environment variables.
    private static final String ACCESS_KEY_ENV = "MINIO_PLAY_ACCESS_KEY";
    private static final String SECRET_KEY_ENV = "MINIO_PLAY_SECRET_KEY";

    static void main() throws IOException {
        String accessKey = System.getenv(ACCESS_KEY_ENV);
        String secretKey = System.getenv(SECRET_KEY_ENV);
        if (StringUtils.isAnyBlank(accessKey, secretKey)) {
            log.error("Environment variables {} and {} are required to run this sample.", ACCESS_KEY_ENV, SECRET_KEY_ENV);
            return;
        }

        AWSCredentials credentials = new StaticAwsCredentials(accessKey, secretKey);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(SERVICE_ENDPOINT, AwsHostNameUtils.parseRegion(SERVICE_ENDPOINT, AmazonS3Client.S3_SERVICE_NAME)))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        try {
            boolean found = s3Client.doesBucketExistV2(BUCKET_NAME);
            if (BooleanUtils.isFalse(found)) {
                s3Client.createBucket(BUCKET_NAME);
            } else {
                log.info("Bucket {} already exists.", BUCKET_NAME);
            }

            log.info("Uploading a new object to S3 from a file");
            File file = new File(UPLOAD_FILE_NAME);

            // Upload file
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, KEY_NAME, file));

            // Download file
            GetObjectRequest rangeObjectRequest = new GetObjectRequest(BUCKET_NAME, KEY_NAME);

            S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

            log.info("Printing bytes retrieved:");

            displayTextInputStream(objectPortion.getObjectContent());

            URL url = s3Client.generatePresignedUrl(BUCKET_NAME, KEY_NAME, Date.from(ZonedDateTime.now().toInstant().atZone(ZoneId.of("GMT+3")).plusMinutes(2).toInstant()));
            log.info("url: {}", url);
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message AmazonServiceException: {}", ase.getMessage());
            log.error("HTTP Status Code: {}", ase.getStatusCode());
            log.error("AWS Error Code:   {}", ase.getErrorCode());
            log.error("Error Type:       {}", ase.getErrorType());
            log.error("Request ID:       {}", ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
            log.error("Error Message AmazonClientException: {}", ace.getMessage());
        }
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null)
                break;
            log.info("    {}", line);
        }
    }
}
