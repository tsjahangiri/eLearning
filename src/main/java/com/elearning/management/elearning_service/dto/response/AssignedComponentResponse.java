package com.elearning.management.elearning_service.dto.response;

import com.elearning.management.elearning_service.domain.AssignmentStatus;
import com.elearning.management.elearning_service.domain.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedComponentResponse {

    private UUID id;
    private String name;
    private ComponentType type;
    private AssignedDatesResponse assignedDates;
    private AssignmentStatus userStatus;
    private String imageUrl;
}

