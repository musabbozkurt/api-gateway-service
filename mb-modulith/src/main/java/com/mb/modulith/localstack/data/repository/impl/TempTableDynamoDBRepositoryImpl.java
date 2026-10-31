package com.mb.modulith.localstack.data.repository.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.mb.modulith.localstack.data.model.TempTable;
import com.mb.modulith.localstack.data.repository.TempTableRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class TempTableDynamoDBRepositoryImpl extends DynamoDBRepository2<TempTable> implements TempTableRepository {

    public TempTableDynamoDBRepositoryImpl(AmazonDynamoDB amazonDynamoDB, DynamoDbClient dynamoDbClient) {
        super(amazonDynamoDB, dynamoDbClient, TempTable.class);
    }
}
