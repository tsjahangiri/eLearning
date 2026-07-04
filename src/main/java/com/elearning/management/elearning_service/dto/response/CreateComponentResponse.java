package com.elearning.management.elearning_service.dto.response;

import com.elearning.management.elearning_service.domain.ComponentCategory;
import com.elearning.management.elearning_service.domain.ComponentType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateComponentResponse(
        UUID id,
        String name,
        ComponentType type,
        ComponentCategory category,
        OffsetDateTime dateCreated
) {}