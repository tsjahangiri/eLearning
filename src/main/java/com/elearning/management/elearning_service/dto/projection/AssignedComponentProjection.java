package com.elearning.management.elearning_service.dto.projection;

import com.elearning.management.elearning_service.domain.AssignmentStatus;
import com.elearning.management.elearning_service.domain.ComponentType;

import java.time.LocalDate;
import java.util.UUID;

public record AssignedComponentProjection(UUID componentId,
                                          String componentName,
                                          ComponentType componentType,
                                          String componentImageUrl,
                                          AssignmentStatus assignmentStatus,
                                          LocalDate assignedStartDate,
                                          LocalDate assignedEndDate) {}
