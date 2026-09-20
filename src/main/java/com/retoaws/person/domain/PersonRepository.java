package com.retoaws.person.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, String> {
    boolean existsByEmailIgnoreCase(String email);
}
