package com.retoaws.person.service;

import com.retoaws.person.api.PersonRequest;
import com.retoaws.person.api.PersonResponse;
import com.retoaws.person.domain.Person;
import com.retoaws.person.domain.PersonRepository;
import com.retoaws.person.error.ConflictException;
import com.retoaws.person.error.PersonNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository repository;

    @Transactional
    public PersonResponse save(PersonRequest request) {
        String identification = request.identification().trim();
        String name = request.name().trim();
        String email = request.email().trim().toLowerCase();

        if (repository.existsById(identification)) {
            throw new ConflictException("A person with this identification already exists");
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A person with this email already exists");
        }

        Person person = Person.builder()
                .identification(identification)
                .name(name)
                .email(email)
                .build();

        return PersonResponse.from(repository.save(person));
    }

    @Transactional(readOnly = true)
    public PersonResponse findByIdentification(String identification) {
        return repository.findById(identification)
                .map(PersonResponse::from)
                .orElseThrow(() -> new PersonNotFoundException(identification));
    }
}
