package com.retoaws.person.error;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(String identification) {
        super("Person not found: " + identification);
    }
}
