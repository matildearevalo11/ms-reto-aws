package com.retoaws.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

public final class CreateUserHandler implements
        RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final UserRepository repository;

    public CreateUserHandler() {
        this(new DynamoDbUserRepository());
    }

    CreateUserHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (event == null || event.getBody() == null || event.getBody().isBlank()) {
            return Responses.error(400, "Request body is required");
        }

        try {
            User user = Responses.objectMapper().readValue(event.getBody(), User.class);
            String validationError = validate(user);
            if (validationError != null) {
                return Responses.error(400, validationError);
            }
            repository.create(user);
            return Responses.json(201, user);
        } catch (ConditionalCheckFailedException exception) {
            return Responses.error(409, "User already exists");
        } catch (JsonProcessingException exception) {
            return Responses.error(400, "Request body must be valid JSON");
        }
    }

    private String validate(User user) {
        if (user.id() == null || user.id().isBlank()) {
            return "id is required";
        }
        if (user.name() == null || user.name().isBlank()) {
            return "name is required";
        }
        if (user.email() == null || user.email().isBlank() || !user.email().contains("@")) {
            return "a valid email is required";
        }
        return null;
    }
}
