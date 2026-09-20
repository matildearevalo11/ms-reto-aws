package com.retoaws.person.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonRequest(
        @NotBlank @Size(max = 30) String identification,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 254) String email) {
}
