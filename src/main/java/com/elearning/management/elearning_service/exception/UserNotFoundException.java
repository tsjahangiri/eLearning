package com.elearning.management.elearning_service.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final String username) {
        super("User not found with username: " + username);
    }
}

