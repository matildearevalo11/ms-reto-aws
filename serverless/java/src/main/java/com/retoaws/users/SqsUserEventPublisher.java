package com.retoaws.users;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

final class SqsUserEventPublisher implements UserEventPublisher {
    private static final String QUEUE_URL_ENVIRONMENT_VARIABLE = "USER_CREATED_QUEUE_URL";

    private final SqsClient sqsClient;
    private final String queueUrl;

    SqsUserEventPublisher() {
        this(SqsClient.create(), requiredEnvironmentVariable(QUEUE_URL_ENVIRONMENT_VARIABLE));
    }

    SqsUserEventPublisher(SqsClient sqsClient, String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void publishCreated(User user) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(Responses.objectMapper().writeValueAsString(user))
                    .build());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to publish the user-created event", exception);
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured");
        }
        return value;
    }
}
