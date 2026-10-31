package com.mb.modulith.localstack.data.repository.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.mb.modulith.localstack.data.model.Car;
import com.mb.modulith.localstack.data.repository.CarRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CarDynamoDBRepositoryImpl extends DynamoDBRepository<Car, String> implements CarRepository {

    public CarDynamoDBRepositoryImpl(AmazonDynamoDB amazonDynamoDB) {
        super(amazonDynamoDB, Car.class);
    }
}
