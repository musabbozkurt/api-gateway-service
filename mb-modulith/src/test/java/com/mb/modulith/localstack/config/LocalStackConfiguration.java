package com.mb.modulith.localstack.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.mb.modulith.localstack.contants.Constants;
import com.mb.modulith.localstack.data.model.Car;
import com.mb.modulith.localstack.data.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class LocalStackConfiguration {

    private static final LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse(Constants.LOCALSTACK_DOCKER_IMAGE))
            .withServices(Service.DYNAMODB.getLocalStackName())
            .withNetworkAliases("localstack")
            .withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));

    static {
        localStack.start();
        System.setProperty("amazon.dynamodb.endpoint", localStack.getEndpoint().toString());
        System.setProperty("aws.accessKeyId", localStack.getAccessKey());
        System.setProperty("aws.secretKey", localStack.getSecretKey());
        System.setProperty("AMAZON_DYNAMODB_ENDPOINT", localStack.getEndpoint().toString());
    }

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @PostConstruct
    public void init() {
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        CreateTableRequest tableUserRequest = dynamoDBMapper.generateCreateTableRequest(User.class);
        tableUserRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        amazonDynamoDB.createTable(tableUserRequest);

        CreateTableRequest tableCarRequest = dynamoDBMapper.generateCreateTableRequest(Car.class);
        tableCarRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        amazonDynamoDB.createTable(tableCarRequest);
    }
}
