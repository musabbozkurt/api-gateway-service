package com.mb.modulith.localstack.data.repository.impl;

import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.data.repository.TempTableRepository4;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@Repository
public class TempTableDynamoDBRepositoryImpl4 extends DynamoDBRepository4<TempTable3> implements TempTableRepository4 {

    public TempTableDynamoDBRepositoryImpl4(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        super(dynamoDbEnhancedAsyncClient, TempTable3.class);
    }
}
