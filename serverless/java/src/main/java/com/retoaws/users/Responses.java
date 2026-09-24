package com.retoaws.users;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

final class Responses {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> JSON_HEADERS = Map.of("Content-Type", "application/json");

    private Responses() {
    }

    static APIGatewayV2HTTPResponse json(int statusCode, Object body) {
        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(JSON_HEADERS)
                    .withBody(OBJECT_MAPPER.writeValueAsString(body))
                    .build();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize Lambda response", exception);
        }
    }

    static APIGatewayV2HTTPResponse error(int statusCode, String message) {
        return json(statusCode, Map.of("message", message));
    }

    static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}
