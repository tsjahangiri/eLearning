package com.elearning.management.elearning_service.controller;

import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import com.elearning.management.elearning_service.security.AuthenticatedUser;
import com.elearning.management.elearning_service.service.ELearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/lms", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "eLearning", description = "eLearning component management API")
public class ELearningController {

    private final ELearningService eLearningService;

    public ELearningController(final ELearningService eLearningService) {
        this.eLearningService = eLearningService;
    }

    @GetMapping("/elearning-components/{componentId}")
    @Operation(summary = "Get detailed information for a specific eLearning component")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Component details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "404", description = "Component not found or not assigned to user"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ComponentDetailResponse> getComponentDetail(
            @PathVariable final UUID componentId,
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(
                eLearningService.getComponentDetail(componentId, authenticatedUser.getUser()));
    }

    @GetMapping("/elearning-components")
    @Operation(summary = "Get all eLearning components assigned to the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned components retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<Page<AssignedComponentResponse>> getAllAssignedComponents(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @PageableDefault(size = 20, sort = "dateCreated", direction = Sort.Direction.DESC)
            final Pageable pageable) {
        return ResponseEntity.ok(
                eLearningService.getAllAssignedComponents(
                        authenticatedUser.getUser(), pageable));
    }
}

