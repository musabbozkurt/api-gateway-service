package com.mb.modulith.localstack.integration_test;

import com.mb.modulith.localstack.base.BaseUnitTest;
import com.mb.modulith.localstack.constants.Constants;
import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.data.repository.impl.DynamoDBRepository4;
import io.reactivex.rxjava3.core.Flowable;
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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.util.List;

@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class AmazonDynamoDBWithPagePublisherTest extends BaseUnitTest {

    @Container
    private static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse(Constants.LOCALSTACK_DOCKER_IMAGE))
            .withServices(Service.DYNAMODB.getLocalStackName())
            .withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));

    private static DynamoDBRepository4<TempTable3> dynamoDBRepository4;
    private static DynamoDbClient dynamoDbClient;
    private static DynamoDbAsyncTable<TempTable3> dynamoDbTable;
    private static PagePublisher<TempTable3> publisher;

    @BeforeAll
    static void setUp() {
        localStackContainer.start();
        System.setProperty("AMAZON_DYNAMODB_ENDPOINT", localStackContainer.getEndpoint().toString());
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = getDynamoDbEnhancedAsyncClient(localStackContainer.getEndpoint(), localStackContainer.getRegion());
        dynamoDbClient = getDynamoDbClient(localStackContainer.getEndpoint(), localStackContainer.getRegion());

        dynamoDbClient
                .listTables()
                .tableNames()
                .forEach(s -> dynamoDbClient.deleteTable(builder -> builder.tableName(s)));

        dynamoDBRepository4 = new DynamoDBRepository4<>(dynamoDbEnhancedAsyncClient, TempTable3.class) {
        };

        dynamoDbTable = dynamoDbEnhancedAsyncClient.table(TempTable3.class.getCanonicalName(), TableSchema.fromClass(TempTable3.class));

        dynamoDbTable.createTable().join();

        dynamoDbClient.createTable(getTempTableNameRequest3());

        getTempTable3Records().forEach(tempTable3 -> dynamoDbTable.putItem(tempTable3).join());

        publisher = dynamoDBRepository4.findAll();
    }

    @AfterAll
    static void afterAll() {
        localStackContainer.stop();
    }

    @Test
    @Order(1)
    void listTables_ShouldReturnTwoTables_WhenTablesAreCreated() {
        // Arrange
        int count = 2;

        // Act
        List<String> tableNames = dynamoDbClient.listTables().tableNames();
        log.info("TableNames: {}, Size: {}", tableNames, tableNames.size());

        // Assertions
        Assertions.assertEquals(count, tableNames.size());
    }

    @Test
    @Order(2)
    void listTables_ShouldContainExpectedTableNames_WhenTablesAreCreated() {
        // Arrange
        // Act
        ListTablesResponse listTablesResponse = dynamoDbClient.listTables();
        List<String> tableNames = listTablesResponse.tableNames();

        log.info("listTablesResponse: {}, tableNames: {}", listTablesResponse, tableNames);

        // Assertions
        Assertions.assertFalse(tableNames.isEmpty());
        Assertions.assertTrue(tableNames.contains("tempTableName"));
        Assertions.assertTrue(tableNames.contains(TempTable3.class.getCanonicalName()));
    }

    @Test
    @Order(3)
    void scan_ShouldReturnItems_WhenTempTable3HasRecords() {
        // Arrange
        // Act
        List<TempTable3> tempTable3s =
                Flowable.fromPublisher(dynamoDbTable.scan())
                        .flatMapIterable(Page::items)
                        .toList()
                        .blockingGet();

        tempTable3s.forEach(tempTable3 -> log.info("tempTable3 id: {}, tempTable3: {}", tempTable3.getId(), tempTable3));

        // Assertions
        Assertions.assertFalse(tempTable3s.isEmpty());
    }

    @Test
    @Order(4)
    void findAll_ShouldContainExpectedTempTable_WhenTempTableWithIdExists() {
        // Arrange
        TempTable3 tempTable3 =
                Flowable.fromPublisher(publisher)
                        .flatMapIterable(Page::items)
                        .toList()
                        .blockingGet()
                        .getFirst();

        // Act
        List<TempTable3> result =
                Flowable.fromPublisher(dynamoDBRepository4.findAll())
                        .flatMapIterable(Page::items)
                        .toList()
                        .blockingGet();

        List<String> stringList = result.stream().map(TempTable3::getId).toList();

        // Assertions
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(stringList.contains(tempTable3.getId()));
    }

    @Test
    @Order(5)
    void deleteAll_ShouldRemoveAllRecords_WhenTempTablesExist() {
        // Arrange
        for (int i = 0; i < 7; i++) {
            getTempTable3Records().forEach(tempTable3 -> dynamoDbTable.putItem(tempTable3).join());
        }
        List<TempTable3> tempTable3s =
                Flowable.fromPublisher(dynamoDBRepository4.findAll())
                        .flatMapIterable(Page::items)
                        .toList()
                        .blockingGet();

        // Act
        dynamoDBRepository4.deleteAll(tempTable3s);

        List<TempTable3> result =
                Flowable.fromPublisher(dynamoDBRepository4.findAll())
                        .flatMapIterable(Page::items)
                        .toList()
                        .blockingGet();

        // Assertions
        Assertions.assertTrue(result.isEmpty());
    }
}
