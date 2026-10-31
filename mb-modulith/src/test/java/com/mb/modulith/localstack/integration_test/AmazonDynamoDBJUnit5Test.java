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
import com.mb.modulith.localstack.data.repository.impl.DynamoDBRepository;
import com.mb.modulith.localstack.data.repository.impl.TempTableDynamoDBRepositoryImpl;
import com.mb.modulith.localstack.data.repository.impl.TempTableDynamoDBRepositoryImpl4;
import com.mb.modulith.localstack.service.TempTableService;
import com.mb.modulith.localstack.service.impl.TempTableServiceImpl;
import com.mb.modulith.localstack.support.DynamoDbLocalStackContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AmazonDynamoDBJUnit5Test extends BaseUnitTest {

    @Container
    private static final DynamoDbLocalStackContainer dynamoDbContainer = new DynamoDbLocalStackContainer(DockerImageName.parse(Constants.LOCALSTACK_DOCKER_IMAGE));

    private static DynamoDBRepository<TempTable, String> tempTableStringDynamoDBRepository;
    private static DynamoDBMapper dynamoDBMapper;

    @BeforeAll
    static void init() {
        dynamoDbContainer.start();
        System.setProperty("AMAZON_DYNAMODB_ENDPOINT", dynamoDbContainer.getEndpointConfiguration().getServiceEndpoint());
        AmazonDynamoDB amazonDynamoDB = dynamoDbContainer.getClient();
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
        final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        TempTableService tempTableService = new TempTableServiceImpl(new TempTableDynamoDBRepositoryImpl(amazonDynamoDB, getDynamoDbClient()), new TempTableDynamoDBRepositoryImpl4(getDynamoDbEnhancedAsyncClient()));
        tempTableStringDynamoDBRepository = new DynamoDBRepository<>(amazonDynamoDB, TempTable.class) {
        };

        CreateTableRequest tempTableRequest = getTempTableRequest(dynamoDBMapper);
        TableUtils.createTableIfNotExists(amazonDynamoDB, tempTableRequest);

        CreateTableRequest tempTableNameRequest = getTempTableNameRequest();
        TableUtils.createTableIfNotExists(amazonDynamoDB, tempTableNameRequest);

        String tempTableTableName = tempTableRequest.getTableName();

        try {
            TableUtils.waitUntilActive(amazonDynamoDB, tempTableTableName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Table table = dynamoDB.getTable(tempTableTableName);
        items().forEach(table::putItem);

        dynamoDBMapper.batchSave(getTempTableRecords());
        List<TempTable> tempTableList = StreamSupport
                .stream(tempTableService.getTempTableRecords().spliterator(), false)
                .toList();
        log.info("tempTableList: {}", tempTableList);
    }

    @AfterAll
    static void afterAll() {
        dynamoDbContainer.stop();
    }

    @Test
    @Order(1)
    void listTables_ShouldReturnTwoTables_WhenTablesAreCreated() {
        // Arrange
        final AmazonDynamoDB tempAmazonDynamoDB = dynamoDbContainer.getClient();

        // Act
        List<String> tableNames = tempAmazonDynamoDB.listTables().getTableNames();
        log.info("TableNames: {}, Size: {}", tableNames, tableNames.size());

        // Assertions
        Assertions.assertEquals(2, tableNames.size());
    }

    @Test
    @Order(2)
    void listTables_ShouldContainExpectedTableNames_WhenTablesAreCreated() {
        // Arrange
        final AmazonDynamoDB tempAmazonDynamoDB = dynamoDbContainer.getClient();

        // Act
        ListTablesResult listTablesResult = tempAmazonDynamoDB.listTables();
        List<String> tableNames = tempAmazonDynamoDB.listTables().getTableNames();

        log.info("listTablesResult: {}, tableNames: {}", listTablesResult, tableNames);

        // Assertions
        Assertions.assertFalse(tableNames.isEmpty());
        Assertions.assertTrue(tableNames.contains("tempTableName"));
        Assertions.assertTrue(tableNames.contains("TempTable"));
    }

    @Test
    @Order(3)
    void scan_ShouldReturnItems_WhenTempTableHasRecords() {
        // Arrange
        final AmazonDynamoDB tempAmazonDynamoDB = dynamoDbContainer.getClient();
        final DynamoDBMapper tempDynamoDBMapper = new DynamoDBMapper(tempAmazonDynamoDB);

        // Act
        PaginatedScanList<TempTable> tempTablePaginatedScanList = tempDynamoDBMapper.scan(TempTable.class, new DynamoDBScanExpression());

        tempTablePaginatedScanList.forEach(tempTable -> log.info("tempTable id: {}, tempTable: {}", tempTable.getId(), tempTable));

        // Assertions
        Assertions.assertFalse(tempTablePaginatedScanList.isEmpty());
    }

    @Test
    @Order(4)
    void deleteAll_ShouldRemoveAllRecords_WhenTempTableRecordsExist() {
        // Arrange
        for (int i = 0; i < 100; i++) {
            dynamoDBMapper.batchSave(getTempTableRecords());
        }
        List<TempTable> tempTables = tempTableStringDynamoDBRepository.findAll();

        // Act
        tempTableStringDynamoDBRepository.deleteAll(tempTables);

        List<TempTable> result = tempTableStringDynamoDBRepository.findAll();

        // Assertions
        Assertions.assertTrue(result.isEmpty());
    }
}
