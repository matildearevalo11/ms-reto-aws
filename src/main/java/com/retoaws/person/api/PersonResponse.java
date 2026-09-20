package com.retoaws.person.api;

import com.retoaws.person.domain.Person;

public record PersonResponse(String identification, String name, String email) {
    public static PersonResponse from(Person person) {
        return new PersonResponse(person.getIdentification(), person.getName(), person.getEmail());
    }
}
