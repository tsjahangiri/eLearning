package com.elearning.management.elearning_service.exception;

import java.util.UUID;

public class ComponentNotFoundException extends RuntimeException {

    public ComponentNotFoundException(final UUID componentId) {
        super("eLearning component not found with id: " + componentId);
    }
}

