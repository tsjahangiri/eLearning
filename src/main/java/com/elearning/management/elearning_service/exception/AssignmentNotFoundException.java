package com.elearning.management.elearning_service.exception;

import java.util.UUID;

public class AssignmentNotFoundException extends RuntimeException {

    public AssignmentNotFoundException(final UUID componentId) {
        super("No assignment found for component id: " + componentId);
    }
}

