package com.retoaws.users;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserHandlersTest {

    @Test
    void getReturnsHardcodedUsers() {
        var response = new GetUsersHandler().handleRequest(new APIGatewayV2HTTPEvent(), null);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Valentina Rojas"));
    }

    @Test
    void createReturnsValidatedUser() {
        var event = new APIGatewayV2HTTPEvent();
        event.setBody("""
                {"id":"1020304050","name":"Camila Restrepo","email":"camila.restrepo@example.com"}
                """);

        var response = new CreateUserHandler().handleRequest(event, null);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("Camila Restrepo"));
    }

    @Test
    void createRejectsInvalidEmail() {
        var event = new APIGatewayV2HTTPEvent();
        event.setBody("""
                {"id":"1020304050","name":"Camila Restrepo","email":"invalid"}
                """);

        assertEquals(400, new CreateUserHandler().handleRequest(event, null).getStatusCode());
    }
}
