package com.mb.modulith.localstack.integration_test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.mb.modulith.localstack.base.BaseUnitTest;
import com.mb.modulith.localstack.contants.Constants;
import com.mb.modulith.localstack.data.model.TempTable;
import com.mb.modulith.localstack.support.DynamoDbLocalStackContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class AmazonDynamoDBTest extends BaseUnitTest {

    @Container
    private static final DynamoDbLocalStackContainer dynamoDbContainer = new DynamoDbLocalStackContainer(DockerImageName.parse(Constants.LOCALSTACK_DOCKER_IMAGE));

    @BeforeAll
    static void init() {
        dynamoDbContainer.start();
        System.setProperty("AMAZON_DYNAMODB_ENDPOINT", dynamoDbContainer.getEndpointConfiguration().getServiceEndpoint());
        final AmazonDynamoDB amazonDynamoDB = dynamoDbContainer.getClient();
        final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

        CreateTableRequest tempTableRequest = getTempTableRequest(new DynamoDBMapper(amazonDynamoDB));
        TableUtils.createTableIfNotExists(amazonDynamoDB, tempTableRequest);

        CreateTableRequest tempTableNameRequest = getTempTableNameRequest();
        TableUtils.createTableIfNotExists(amazonDynamoDB, tempTableNameRequest);

        String tempTableRequestTableName = tempTableRequest.getTableName();

        try {
            TableUtils.waitUntilActive(amazonDynamoDB, tempTableRequestTableName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Table table = dynamoDB.getTable(tempTableRequestTableName);
        items().forEach(table::putItem);
    }

    @Test
    void listTables_ShouldReturnTwoTables_WhenTablesAreCreated() {
        // Arrange
        final AmazonDynamoDB amazonDynamoDB = dynamoDbContainer.getClient();

        // Act
        List<String> tableNames = amazonDynamoDB.listTables().getTableNames();
        log.info("TableNames: {}, Size: {}", tableNames, tableNames.size());

        // Assertions
        Assertions.assertEquals(2, tableNames.size());
    }

    @Test
    void listTables_ShouldContainExpectedTableNames_WhenTablesAreCreated() {
        // Arrange
        final AmazonDynamoDB amazonDynamoDB = dynamoDbContainer.getClient();

        // Act
        ListTablesResult listTablesResult = amazonDynamoDB.listTables();
        List<String> tableNames = amazonDynamoDB.listTables().getTableNames();

        log.info("listTablesResult: {}, tableNames: {}", listTablesResult, tableNames);

        // Assertions
        Assertions.assertFalse(tableNames.isEmpty());
        Assertions.assertTrue(tableNames.contains("tempTableName"));
        Assertions.assertTrue(tableNames.contains("TempTable"));
    }

    @Test
    void scan_ShouldReturnItems_WhenTempTableHasRecords() {
        // Arrange
        final AmazonDynamoDB amazonDynamoDB = dynamoDbContainer.getClient();

        final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        // Act
        PaginatedScanList<TempTable> tempTablePaginatedScanList = dynamoDBMapper.scan(TempTable.class, new DynamoDBScanExpression());

        tempTablePaginatedScanList.forEach(tempTable -> log.info("TempTable id: {}, TempTable: {}", tempTable.getId(), tempTable));

        // Assertions
        Assertions.assertFalse(tempTablePaginatedScanList.isEmpty());
    }
}
