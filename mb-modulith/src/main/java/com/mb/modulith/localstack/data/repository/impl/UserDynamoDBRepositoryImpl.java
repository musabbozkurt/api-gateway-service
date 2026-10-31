package com.mb.modulith.localstack.data.repository.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.mb.modulith.localstack.data.model.User;
import com.mb.modulith.localstack.data.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserDynamoDBRepositoryImpl extends DynamoDBRepository<User, String> implements UserRepository {

    public UserDynamoDBRepositoryImpl(AmazonDynamoDB amazonDynamoDB) {
        super(amazonDynamoDB, User.class);
    }
}
