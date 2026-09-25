package com.retoaws.users;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;

class UserHandlersTest {

    private final FakeUserRepository repository = new FakeUserRepository();
    private final FakeUserEventPublisher eventPublisher = new FakeUserEventPublisher();

    @Test
    void getReturnsHardcodedUsers() {
        repository.users.add(new User("1018456789", "Valentina Rojas", "valentina@example.com"));
        var response = new GetUsersHandler(repository).handleRequest(new APIGatewayV2HTTPEvent(), null);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Valentina Rojas"));
    }

    @Test
    void createReturnsValidatedUser() {
        var event = new APIGatewayV2HTTPEvent();
        event.setBody("""
                {"id":"1020304050","name":"Camila Restrepo","email":"camila.restrepo@example.com"}
                """);

        var response = new CreateUserHandler(repository, eventPublisher).handleRequest(event, null);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("Camila Restrepo"));
        assertEquals(1, repository.users.size());
        assertEquals(1, eventPublisher.users.size());
    }

    @Test
    void createRejectsInvalidEmail() {
        var event = new APIGatewayV2HTTPEvent();
        event.setBody("""
                {"id":"1020304050","name":"Camila Restrepo","email":"invalid"}
                """);

        assertEquals(400, new CreateUserHandler(repository, eventPublisher).handleRequest(event, null).getStatusCode());
        assertTrue(eventPublisher.users.isEmpty());
    }

    private static final class FakeUserRepository implements UserRepository {
        private final List<User> users = new ArrayList<>();

        @Override
        public List<User> findAll() {
            return List.copyOf(users);
        }

        @Override
        public void create(User user) {
            users.add(user);
        }
    }

    private static final class FakeUserEventPublisher implements UserEventPublisher {
        private final List<User> users = new ArrayList<>();

        @Override
        public void publishCreated(User user) {
            users.add(user);
        }
    }
}
