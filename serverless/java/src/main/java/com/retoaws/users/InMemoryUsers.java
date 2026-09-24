package com.retoaws.users;

import java.util.List;

final class InMemoryUsers {

    private static final List<User> USERS = List.of(
            new User("1018456789", "Valentina Rojas", "valentina.rojas@example.com"),
            new User("1032567890", "Santiago Mejía", "santiago.mejia@example.com"),
            new User("1143987654", "Mariana Torres", "mariana.torres@example.com")
    );

    private InMemoryUsers() {
    }

    static List<User> all() {
        return USERS;
    }
}
