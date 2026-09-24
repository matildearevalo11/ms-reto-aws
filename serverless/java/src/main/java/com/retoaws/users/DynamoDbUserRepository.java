package com.retoaws.users;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.List;
import java.util.Map;

final class DynamoDbUserRepository implements UserRepository {

    private static final DynamoDbClient CLIENT = DynamoDbClient.create();
    private final String tableName;

    DynamoDbUserRepository() {
        this(requiredEnvironmentVariable("USERS_TABLE"));
    }

    DynamoDbUserRepository(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public List<User> findAll() {
        return CLIENT.scan(ScanRequest.builder().tableName(tableName).build())
                .items()
                .stream()
                .map(DynamoDbUserRepository::toUser)
                .toList();
    }

    @Override
    public void create(User user) {
        CLIENT.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toItem(user))
                .conditionExpression("attribute_not_exists(id)")
                .build());
    }

    private static Map<String, AttributeValue> toItem(User user) {
        return Map.of(
                "id", AttributeValue.fromS(user.id()),
                "nombre", AttributeValue.fromS(user.name()),
                "email", AttributeValue.fromS(user.email())
        );
    }

    private static User toUser(Map<String, AttributeValue> item) {
        return new User(item.get("id").s(), item.get("nombre").s(), item.get("email").s());
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " environment variable is required");
        }
        return value;
    }
}
