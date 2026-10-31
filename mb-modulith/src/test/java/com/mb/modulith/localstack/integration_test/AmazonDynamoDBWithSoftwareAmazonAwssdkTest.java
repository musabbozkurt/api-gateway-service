package com.mb.modulith.localstack.integration_test;

import com.mb.modulith.localstack.base.BaseUnitTest;
import com.mb.modulith.localstack.contants.Constants;
import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.data.repository.impl.DynamoDBRepository3;
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
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.util.List;

@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class AmazonDynamoDBWithSoftwareAmazonAwssdkTest extends BaseUnitTest {

    @Container
    private static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse(Constants.LOCALSTACK_DOCKER_IMAGE))
            .withServices(Service.DYNAMODB.getLocalStackName())
            .withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));

    private static DynamoDBRepository3<TempTable3> tempTableStringDynamoDBRepository3;
    private static DynamoDbClient dynamoDbClient;
    private static DynamoDbTable<TempTable3> dynamoDbTable;

    private static PageIterable<TempTable3> tablePageIterable;

    @BeforeAll
    static void setUp() {
        localStackContainer.start();
        System.setProperty("AMAZON_DYNAMODB_ENDPOINT", localStackContainer.getEndpoint().toString());
        DynamoDbEnhancedClient dynamoDbEnhancedClient = getDynamoClient(localStackContainer.getEndpoint(), localStackContainer.getRegion());
        dynamoDbClient = getDynamoDbClient(localStackContainer.getEndpoint(), localStackContainer.getRegion());

        tempTableStringDynamoDBRepository3 = new DynamoDBRepository3<>(dynamoDbEnhancedClient, TempTable3.class) {
        };
        dynamoDbTable = dynamoDbEnhancedClient.table(TempTable3.class.getCanonicalName(), TableSchema.fromClass(TempTable3.class));
        dynamoDbTable.createTable();
        dynamoDbClient.createTable(getTempTableNameRequest3());

        getTempTable3Records().forEach(dynamoDbTable::putItem);

        tablePageIterable = tempTableStringDynamoDBRepository3.findAll();
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
        SdkIterable<TempTable3> items = dynamoDbTable.scan().items();

        items.forEach(tempTable -> log.info("tempTable id: {}, tempTable: {}", tempTable.getId(), tempTable));

        // Assertions
        Assertions.assertTrue(items.iterator().hasNext());
    }

    @Test
    @Order(4)
    void findAll_ShouldContainExpectedTempTable_WhenTempTableWithIdExists() {
        // Arrange
        TempTable3 tempTable = tablePageIterable.items().iterator().next();

        // Act
        SdkIterable<TempTable3> result = tempTableStringDynamoDBRepository3.findAll().items();

        List<String> ids = result.stream().map(TempTable3::getId).toList();

        // Assertions
        Assertions.assertNotNull(result);
        Assertions.assertTrue(ids.contains(tempTable.getId()));
    }

    @Test
    @Order(5)
    void deleteAll_ShouldRemoveAllRecords_WhenTempTablesExist() {
        // Arrange
        for (int i = 0; i < 7; i++) {
            getTempTable3Records().forEach(dynamoDbTable::putItem);
        }
        PageIterable<TempTable3> pageIterable = tempTableStringDynamoDBRepository3.findAll();

        // Act
        tempTableStringDynamoDBRepository3.deleteAll(pageIterable.items().stream().toList());

        PageIterable<TempTable3> result = tempTableStringDynamoDBRepository3.findAll();

        // Assertions
        Assertions.assertFalse(result.items().iterator().hasNext());
    }
}
