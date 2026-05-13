package com.elearning.management.elearning_service.dto.request;

import com.elearning.management.elearning_service.domain.AssignmentStatus;
import com.elearning.management.elearning_service.domain.ComponentCategory;
import com.elearning.management.elearning_service.domain.ComponentType;

public record ComponentFilter(
        AssignmentStatus status,
        ComponentType type,
        ComponentCategory category) {

    public boolean isEmpty() {
        return status == null && type == null && category == null;
    }
}
