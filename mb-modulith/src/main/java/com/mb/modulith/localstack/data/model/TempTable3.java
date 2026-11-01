package com.mb.modulith.localstack.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;

@Getter
@Setter
@ToString
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
public class TempTable3 implements Serializable {

    @Id
    @JsonIgnore
    @Getter(onMethod = @__({@DynamoDbPartitionKey}))
    private String id;

    @Getter(onMethod = @__({@DynamoDbAttribute("Color")}))
    private String color;

    @Getter(onMethod = @__({@DynamoDbAttribute("Status")}))
    private Boolean status;
}
