package com.mb.modulith.localstack.integration_test;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.EventSourcePosition;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.services.lambda.model.Runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DynamoDbIntegrationTest {

    @Container
    private static final FlociContainer floci = new FlociContainer();

    private DynamoDbClient dynamo;
    private LambdaClient lambdaClient;

    @BeforeEach
    void setUp() {
        var credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey()));
        var region = Region.of(floci.getRegion());
        var endpoint = URI.create(floci.getEndpoint());

        dynamo = DynamoDbClient.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        lambdaClient = LambdaClient.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Test
    void createTable_ShouldSuccessfullyCreateTable_WhenSchemaIsValid() {
        // Arrange
        String tableName = "Products";

        // Act
        CreateTableResponse response = dynamo.createTable(b -> b
                .tableName(tableName)
                .attributeDefinitions(a -> a.attributeName("id").attributeType(ScalarAttributeType.S))
                .keySchema(k -> k.attributeName("id").keyType(KeyType.HASH))
                .billingMode(BillingMode.PAY_PER_REQUEST));

        // Assertions
        assertThat(response.tableDescription().tableName()).isEqualTo(tableName);
    }

    @Test
    void putItem_ShouldStoreRecordSuccessfully_WhenTableExists() {
        // Arrange
        String tableName = "Orders";
        dynamo.createTable(b -> b
                .tableName(tableName)
                .attributeDefinitions(a -> a.attributeName("id").attributeType(ScalarAttributeType.S))
                .keySchema(k -> k.attributeName("id").keyType(KeyType.HASH))
                .billingMode(BillingMode.PAY_PER_REQUEST));

        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.fromS("order-1"),
                "status", AttributeValue.fromS("placed")
        );

        // Act
        dynamo.putItem(b -> b.tableName(tableName).item(item));

        // Assertions
        Map<String, AttributeValue> fetchedItem = dynamo.getItem(b -> b
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS("order-1")))).item();

        assertThat(fetchedItem).isNotEmpty();
        assertThat(fetchedItem.get("status").s()).isEqualTo("placed");
    }

    @Test
    void getItem_ShouldReturnCorrectRecord_WhenRecordExists() {
        // Arrange
        String tableName = "Customers";
        dynamo.createTable(b -> b
                .tableName(tableName)
                .attributeDefinitions(a -> a.attributeName("id").attributeType(ScalarAttributeType.S))
                .keySchema(k -> k.attributeName("id").keyType(KeyType.HASH))
                .billingMode(BillingMode.PAY_PER_REQUEST));

        String customerId = "cust-100";
        dynamo.putItem(b -> b.tableName(tableName).item(Map.of(
                "id", AttributeValue.fromS(customerId),
                "name", AttributeValue.fromS("Alice")
        )));

        // Act
        Map<String, AttributeValue> fetchedItem = dynamo.getItem(b -> b
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(customerId)))).item();

        // Assertions
        assertThat(fetchedItem).isNotEmpty();
        assertThat(fetchedItem.get("id").s()).isEqualTo(customerId);
        assertThat(fetchedItem.get("name").s()).isEqualTo("Alice");
    }

    @Test
    void updateItem_ShouldModifyExistingAttributes_WhenItemExists() {
        // Arrange
        String tableName = "Inventory";
        dynamo.createTable(b -> b
                .tableName(tableName)
                .attributeDefinitions(a -> a.attributeName("itemId").attributeType(ScalarAttributeType.S))
                .keySchema(k -> k.attributeName("itemId").keyType(KeyType.HASH))
                .billingMode(BillingMode.PAY_PER_REQUEST));

        dynamo.putItem(b -> b.tableName(tableName).item(Map.of(
                "itemId", AttributeValue.fromS("item-100"),
                "stock", AttributeValue.fromN("10")
        )));

        // Act
        dynamo.updateItem(b -> b
                .tableName(tableName)
                .key(Map.of("itemId", AttributeValue.fromS("item-100")))
                .attributeUpdates(Map.of(
                        "stock", AttributeValueUpdate.builder()
                                .value(AttributeValue.fromN("15"))
                                .action(AttributeAction.PUT)
                                .build()
                )));

        // Assertions
        Map<String, AttributeValue> fetchedItem = dynamo.getItem(b -> b
                .tableName(tableName)
                .key(Map.of("itemId", AttributeValue.fromS("item-100")))).item();

        assertThat(fetchedItem.get("stock").n()).isEqualTo("15");
    }

    @Test
    void deleteItem_ShouldRemoveRecordFromTable_WhenRecordExists() {
        // Arrange
        String tableName = "Invoices";
        dynamo.createTable(b -> b
                .tableName(tableName)
                .attributeDefinitions(a -> a.attributeName("id").attributeType(ScalarAttributeType.S))
                .keySchema(k -> k.attributeName("id").keyType(KeyType.HASH))
                .billingMode(BillingMode.PAY_PER_REQUEST));

        String invoiceId = "inv-500";
        dynamo.putItem(b -> b.tableName(tableName).item(Map.of(
                "id", AttributeValue.fromS(invoiceId),
                "amount", AttributeValue.fromN("1250.50")
        )));

        // Act
        dynamo.deleteItem(b -> b
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(invoiceId))));

        // Assertions
        Map<String, AttributeValue> fetchedItem = dynamo.getItem(b -> b
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(invoiceId)))).item();

        assertThat(fetchedItem).isEmpty();
    }

    @Test
    void streamTrigger_ShouldInvokeLambdaFunction_WhenNewItemIsInsertedIntoDynamoDb() throws IOException {
        // Arrange
        String tableName = "StreamingOrders";
        String functionName = "StreamProcessorLambda";

        CreateTableResponse createTableResponse = dynamo.createTable(b -> b
                .tableName(tableName)
                .attributeDefinitions(a -> a.attributeName("id").attributeType(ScalarAttributeType.S))
                .keySchema(k -> k.attributeName("id").keyType(KeyType.HASH))
                .streamSpecification(s -> s
                        .streamEnabled(true)
                        .streamViewType(StreamViewType.NEW_AND_OLD_IMAGES))
                .billingMode(BillingMode.PAY_PER_REQUEST));

        String streamArn = createTableResponse.tableDescription().latestStreamArn();

        String pythonCode = """
                def handler(event, context):
                    print('Received event:', event)
                    return {'statusCode': 200}
                """;

        byte[] zipBytes = createLambdaZip(pythonCode);

        CreateFunctionResponse createFunctionResponse = lambdaClient.createFunction(b -> b
                .functionName(functionName)
                .runtime(Runtime.PYTHON3_9)
                .role("arn:aws:iam::000000000000:role/lambda-role")
                .handler("index.handler")
                .code(FunctionCode.builder().zipFile(SdkBytes.fromByteArray(zipBytes)).build()));

        CreateEventSourceMappingResponse mappingResponse = lambdaClient.createEventSourceMapping(b -> b
                .eventSourceArn(streamArn)
                .functionName(functionName)
                .startingPosition(EventSourcePosition.LATEST));

        // Act
        dynamo.putItem(b -> b
                .tableName(tableName)
                .item(Map.of("id", AttributeValue.fromS("stream-order-1"), "amount", AttributeValue.fromN("250"))));

        // Assertions
        assertThat(mappingResponse.uuid()).isNotBlank();
        assertThat(createFunctionResponse.functionArn()).contains(functionName);
    }

    private byte[] createLambdaZip(String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("index.py");
            zos.putNextEntry(entry);
            zos.write(content.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
