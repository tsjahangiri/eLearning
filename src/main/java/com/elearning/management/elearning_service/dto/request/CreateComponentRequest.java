package com.elearning.management.elearning_service.dto.request;

import com.elearning.management.elearning_service.domain.ComponentCategory;
import com.elearning.management.elearning_service.domain.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateComponentRequest(

        @NotBlank
        String name,

        String description,

        @NotNull
        ComponentType type,

        String imageUrl,

        @Positive
        Integer durationInMinutes,

        ComponentCategory category,

        LocalDate availableStartDate,

        LocalDate availableEndDate
) {}