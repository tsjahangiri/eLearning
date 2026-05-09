package com.elearning.management.elearning_service.controller;

import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import com.elearning.management.elearning_service.service.ELearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/project/restapi/lms", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "eLearning", description = "eLearning component management API")
public class ELearningController {

    private final ELearningService eLearningService;

    public ELearningController(final ELearningService eLearningService) {
        this.eLearningService = eLearningService;
    }

    @GetMapping("/my-elearnings/{componentId}")
    @Operation(summary = "Get detailed information for a specific eLearning component")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Component details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "404", description = "Component not found or not assigned to user"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ComponentDetailResponse> getComponentDetail(
            @PathVariable final UUID componentId,
            @AuthenticationPrincipal final UserDetails userDetails) {
        return ResponseEntity.ok(
                eLearningService.getComponentDetail(componentId, userDetails.getUsername()));
    }
}

