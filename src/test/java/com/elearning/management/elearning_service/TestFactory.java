package com.elearning.management.elearning_service;

import com.elearning.management.elearning_service.domain.*;
import com.elearning.management.elearning_service.dto.projection.AssignedComponentProjection;
import com.elearning.management.elearning_service.dto.request.CreateComponentRequest;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TestFactory {

    public static final String DEFAULT_USERNAME = "testuser";
    public static final String DEFAULT_PASSWORD = "P4ssword!";
    public static final String DEFAULT_COMPONENT_NAME = "Introduction to Scrum";
    public static final ComponentType DEFAULT_TYPE = ComponentType.COURSE;
    public static final AssignmentStatus DEFAULT_STATUS = AssignmentStatus.BOOKED;
    public static final ComponentCategory DEFAULT_CATEGORY =
            ComponentCategory.SOFTWARE_DEVELOPMENT;

    private TestFactory() {
    }

    // ─── Domain builders ───────────────────────────────────────────────────

    public static User buildUser(final String encodedPassword) {
        final User user = new User();
        user.setUsername(DEFAULT_USERNAME);
        user.setPassword(encodedPassword);
        user.setEmail("testuser@example.com");
        user.setRole(UserRole.ROLE_USER);
        return user;
    }

    public static ELearningComponent buildComponent() {
        final ELearningComponent component = new ELearningComponent();
        component.setName(DEFAULT_COMPONENT_NAME);
        component.setDescription(
                "A comprehensive introduction to Scrum framework");
        component.setType(DEFAULT_TYPE);
        component.setImageUrl("http://example.com/scrum.jpg");
        component.setDurationInMinutes(480);
        component.setCategory(DEFAULT_CATEGORY);
        component.setAvailableStartDate(LocalDate.of(2024, 1, 1));
        component.setAvailableEndDate(LocalDate.of(2024, 12, 31));
        return component;
    }

    public static ELearningComponent buildComponent(final String name,
                                                    final ComponentType type) {
        final ELearningComponent component = buildComponent();
        component.setName(name);
        component.setType(type);
        return component;
    }

    public static UserAssignment buildAssignment(final User user,
                                                 final ELearningComponent component) {
        final UserAssignment assignment = new UserAssignment();
        assignment.setUser(user);
        assignment.setComponent(component);
        assignment.setStatus(DEFAULT_STATUS);
        assignment.setAssignedStartDate(LocalDate.of(2024, 1, 15));
        assignment.setAssignedEndDate(LocalDate.of(2024, 6, 15));
        return assignment;
    }

    public static UserAssignment buildAssignment(final User user,
                                                 final ELearningComponent component,
                                                 final AssignmentStatus status) {
        final UserAssignment assignment = buildAssignment(user, component);
        assignment.setStatus(status);
        return assignment;
    }

    // ─── Projection builder ────────────────────────────────────────────────

    public static AssignedComponentProjection buildProjection() {
        return new AssignedComponentProjection(
                UUID.randomUUID(),
                DEFAULT_COMPONENT_NAME,
                DEFAULT_TYPE,
                "http://example.com/scrum.jpg",
                DEFAULT_STATUS,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 6, 15));
    }

    public static AssignedComponentProjection buildProjection(
            final String name,
            final ComponentType type,
            final AssignmentStatus status) {
        return new AssignedComponentProjection(
                UUID.randomUUID(),
                name,
                type,
                "http://example.com/image.jpg",
                status,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 6, 15));
    }

    // ─── Request builders ──────────────────────────────────────────────────

    public static CreateComponentRequest buildCreateComponentRequest() {
        return new CreateComponentRequest(
                DEFAULT_COMPONENT_NAME,
                "A comprehensive introduction to Scrum framework",
                DEFAULT_TYPE,
                "http://example.com/scrum.jpg",
                480,
                DEFAULT_CATEGORY,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
    }

    public static CreateComponentRequest buildMinimalCreateComponentRequest() {
        return new CreateComponentRequest(
                DEFAULT_COMPONENT_NAME,
                null,
                DEFAULT_TYPE,
                null,
                null,
                null,
                null,
                null);
    }

    // ─── Response helpers ──────────────────────────────────────────────────

    public static class AssignedComponentPage {
        public List<AssignedComponentResponse> content;
        public int totalElements;
        public int totalPages;
        public int size;
        public int number;
    }
}

