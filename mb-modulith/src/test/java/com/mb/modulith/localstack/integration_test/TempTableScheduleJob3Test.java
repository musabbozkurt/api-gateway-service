package com.mb.modulith.localstack.integration_test;

import com.mb.modulith.localstack.base.BaseUnitTest;
import com.mb.modulith.localstack.constants.Constants;
import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.data.repository.impl.TempTableDynamoDBRepositoryImpl;
import com.mb.modulith.localstack.data.repository.impl.TempTableDynamoDBRepositoryImpl4;
import com.mb.modulith.localstack.scheduler.TempTableScheduleJob3;
import com.mb.modulith.localstack.service.TempTableService;
import com.mb.modulith.localstack.service.impl.MetricService;
import com.mb.modulith.localstack.service.impl.TempTableServiceImpl;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;

@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class TempTableScheduleJob3Test extends BaseUnitTest {

    @Container
    private static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse(Constants.LOCALSTACK_DOCKER_IMAGE))
            .withServices(Service.DYNAMODB.getLocalStackName())
            .withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));

    private static DynamoDbAsyncClient dynamoDbAsyncClient;
    private static TempTableService tempTableService;
    private static TempTableScheduleJob3 tempTableScheduleJob3;
    private static DynamoDbAsyncTable<TempTable3> dynamoDbTable;

    @BeforeAll
    static void setUp() {
        localStackContainer.start();
        System.setProperty("aws.accessKeyId", localStackContainer.getAccessKey());
        System.setProperty("aws.secretKey", localStackContainer.getSecretKey());
        System.setProperty("AMAZON_DYNAMODB_ENDPOINT", localStackContainer.getEndpoint().toString());
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = getDynamoDbEnhancedAsyncClient(localStackContainer.getEndpoint(), localStackContainer.getRegion());
        dynamoDbAsyncClient = getDynamoDbAsyncClient(localStackContainer.getEndpoint(), localStackContainer.getRegion());

        dynamoDbAsyncClient
                .listTables()
                .join()
                .tableNames()
                .forEach(s -> dynamoDbAsyncClient.deleteTable(builder -> builder.tableName(s)));

        dynamoDbTable = dynamoDbEnhancedAsyncClient.table(TempTable3.class.getCanonicalName(), TableSchema.fromClass(TempTable3.class));
        tempTableService = new TempTableServiceImpl(new TempTableDynamoDBRepositoryImpl(amazonDynamoDB(localStackContainer.getEndpoint().toString(), localStackContainer.getRegion()), getDynamoDbClient()), new TempTableDynamoDBRepositoryImpl4(dynamoDbEnhancedAsyncClient));
        MetricService metricService = new MetricService(new CompositeMeterRegistry());

        tempTableScheduleJob3 = new TempTableScheduleJob3(tempTableService, metricService);

        dynamoDbTable.createTable().join();

        dynamoDbAsyncClient.createTable(getTempTableNameRequest3());

        getTempTable3Records().forEach(tempTable3 -> dynamoDbTable.putItem(tempTable3).join());
    }

    @AfterAll
    static void afterAll() {
        localStackContainer.stop();
    }

    @Test
    @Order(1)
    void scheduleTempTableDeletion_ShouldDeleteRecordsWithColor_WhenTempTable3sExist() {
        // Arrange
        List<TempTable3> tempTable3s = getTempTable3s(tempTableService.getRecords());

        Assertions.assertFalse(tempTable3s.isEmpty());
        Assertions.assertEquals(3, tempTable3s.size());

        // Act
        tempTableScheduleJob3.scheduleTempTableDeletion();

        // Assertions
        // Async part should be completed before the latest result is asserted, that's why the assertion is awaited.
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<TempTable3> result = getTempTable3s(tempTableService.getRecords());
                    Assertions.assertTrue(result.isEmpty());
                });
    }
}
