package com.retoaws.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.Map;

public final class GetUsersHandler implements
        RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final UserRepository repository;

    public GetUsersHandler() {
        this(new DynamoDbUserRepository());
    }

    GetUsersHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        return Responses.json(200, Map.of("users", repository.findAll()));
    }
}
