package com.elearning.management.elearning_service.exception;

import java.time.Instant;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String message;
    private final Instant timestamp;

    public ErrorResponse(final String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }
}

