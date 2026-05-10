package com.elearning.management.elearning_service.dto.response;

import com.elearning.management.elearning_service.domain.AssignmentStatus;
import com.elearning.management.elearning_service.domain.ComponentCategory;
import com.elearning.management.elearning_service.domain.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private ComponentType type;
    private AvailableDatesResponse availableDates;
    private String imageUrl;
    private Set<String> metaTags;
    private AssignmentStatus userStatus;
    private String duration;
    private ComponentCategory category;
}

