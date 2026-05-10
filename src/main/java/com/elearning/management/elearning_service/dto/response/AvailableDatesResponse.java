package com.elearning.management.elearning_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableDatesResponse {

    private LocalDate startDate;
    private LocalDate endDate;
}

