package com.example.springchatapp.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DynamoService {

    private AmazonDynamoDB amazonDynamoDB;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    @Autowired
    public DynamoService(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    public void saveMessage(String userId, String text, long timestamp) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        Table table = dynamoDB.getTable(tableName);

        table.putItem(new Item()
                .withPrimaryKey("room", "default")
                .withLong("createAt", timestamp)
                .withString("user", userId)
                .withString("text", text));
    }
}
