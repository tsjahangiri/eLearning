package com.elearning.management.elearning_service.controller;

import com.elearning.management.elearning_service.BaseIntegrationTest;
import com.elearning.management.elearning_service.TestFactory;
import com.elearning.management.elearning_service.domain.*;
import com.elearning.management.elearning_service.dto.request.CreateComponentRequest;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import com.elearning.management.elearning_service.dto.response.CreateComponentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elearning.management.elearning_service.TestFactory.buildComponent;
import static org.assertj.core.api.Assertions.assertThat;

class ELearningControllerIT extends BaseIntegrationTest {

    // ─── CREATE COMPONENT ──────────────────────────────────────────────────

    @Test
    void createComponent_validRequest_returns201() {
        final CreateComponentRequest request =
                TestFactory.buildCreateComponentRequest();

        final ResponseEntity<CreateComponentResponse> response =
                authenticatedRestTemplate().postForEntity(
                        baseUrl(), request, CreateComponentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createComponent_validRequest_returnsResponseBody() {
        final CreateComponentRequest request =
                TestFactory.buildCreateComponentRequest();

        final ResponseEntity<CreateComponentResponse> response =
                authenticatedRestTemplate().postForEntity(
                        baseUrl(), request, CreateComponentResponse.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name())
                .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
        assertThat(response.getBody().type()).isEqualTo(TestFactory.DEFAULT_TYPE);
        assertThat(response.getBody().category())
                .isEqualTo(TestFactory.DEFAULT_CATEGORY);
        assertThat(response.getBody().dateCreated()).isNotNull();
    }

    @Test
    void createComponent_validRequest_returnsLocationHeader() {
        final CreateComponentRequest request =
                TestFactory.buildCreateComponentRequest();

        final ResponseEntity<CreateComponentResponse> response =
                authenticatedRestTemplate().postForEntity(
                        baseUrl(), request, CreateComponentResponse.class);

        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString())
                .contains(response.getBody().id().toString());
    }

    @Test
    void createComponent_validRequest_persistedInDatabase() {
        final CreateComponentRequest request =
                TestFactory.buildCreateComponentRequest();

        final ResponseEntity<CreateComponentResponse> response =
                authenticatedRestTemplate().postForEntity(
                        baseUrl(), request, CreateComponentResponse.class);

        assertThat(componentRepository.findById(response.getBody().id()))
                .isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getName())
                            .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
                    assertThat(c.getType()).isEqualTo(TestFactory.DEFAULT_TYPE);
                    assertThat(c.getCategory())
                            .isEqualTo(TestFactory.DEFAULT_CATEGORY);
                });
    }

    @Test
    void createComponent_minimalRequest_returns201() {
        final CreateComponentRequest request =
                TestFactory.buildMinimalCreateComponentRequest();

        final ResponseEntity<CreateComponentResponse> response =
                authenticatedRestTemplate().postForEntity(
                        baseUrl(), request, CreateComponentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().category()).isNull();
    }

    @Test
    void createComponent_missingName_returns400() {
        final CreateComponentRequest request = new CreateComponentRequest(
                "", null, ComponentType.COURSE, null, null, null, null, null);

        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createComponent_missingType_returns400() {
        final CreateComponentRequest request = new CreateComponentRequest(
                "Some Course", null, null, null, null, null, null, null);

        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createComponent_negativeDuration_returns400() {
        final CreateComponentRequest request = new CreateComponentRequest(
                "Some Course", null, ComponentType.COURSE,
                null, -30, null, null, null);

        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createComponent_noAuthentication_returns401() {
        final CreateComponentRequest request =
                TestFactory.buildCreateComponentRequest();

        final ResponseEntity<Void> response = restTemplate
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createComponent_wrongPassword_returns401() {
        final CreateComponentRequest request =
                TestFactory.buildCreateComponentRequest();

        final ResponseEntity<Void> response = restTemplate
                .withBasicAuth(TestFactory.DEFAULT_USERNAME, "wrongpassword")
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── GET ALL ASSIGNED COMPONENTS ───────────────────────────────────────

    @Test
    void getAllAssignedComponents_authenticatedUser_returns200() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content).hasSize(1);
        assertThat(response.getBody().totalElements).isEqualTo(1);
    }

    @Test
    void getAllAssignedComponents_userHasNoAssignments_returnsEmptyPage() {
        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content).isEmpty();
        assertThat(response.getBody().totalElements).isEqualTo(0);
    }

    @Test
    void getAllAssignedComponents_multipleAssignments_returnsAll() {
        saveAssignment(saveComponent());
        saveAssignment(saveComponent("Agile Video", ComponentType.MEDIA));

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(2);
        assertThat(response.getBody().totalElements).isEqualTo(2);
    }

    @Test
    void getAllAssignedComponents_paginationParams_returnsCorrectPage() {
        saveAssignment(saveComponent());
        saveAssignment(saveComponent("Agile Video", ComponentType.MEDIA));
        saveAssignment(saveComponent("Java Basics", ComponentType.COURSE));

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "?page=0&size=2",
                        TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(2);
        assertThat(response.getBody().totalElements).isEqualTo(3);
        assertThat(response.getBody().totalPages).isEqualTo(2);
    }

    @Test
    void getAllAssignedComponents_correctStatusReturned() {
        saveAssignment(saveComponent(), AssignmentStatus.IN_PROGRESS);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content.get(0).getUserStatus())
                .isEqualTo(AssignmentStatus.IN_PROGRESS);
    }

    @Test
    void getAllAssignedComponents_correctComponentNameReturned() {
        saveAssignment(saveComponent());

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getBody().content.get(0).getName())
                .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
    }

    @Test
    void getAllAssignedComponents_onlyReturnsCurrentUserAssignments() {
        saveAssignment(saveComponent());

        final User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("OtherPass!"));
        otherUser.setEmail("other@example.com");
        otherUser.setRole(UserRole.ROLE_USER);
        final User savedOtherUser = userRepository.save(otherUser);
        final ELearningComponent otherComponent = saveComponent(
                "Other Course", ComponentType.MEDIA);
        assignmentRepository.save(
                TestFactory.buildAssignment(savedOtherUser, otherComponent));

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(1);
        assertThat(response.getBody().content.get(0).getName())
                .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
    }

    @Test
    void getAllAssignedComponents_noAuthentication_returns401() {
        final ResponseEntity<Void> response = restTemplate
                .getForEntity(baseUrl(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getAllAssignedComponents_wrongPassword_returns401() {
        final ResponseEntity<Void> response = restTemplate
                .withBasicAuth(TestFactory.DEFAULT_USERNAME, "wrongpassword")
                .getForEntity(baseUrl(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getAllAssignedComponents_wrongUsername_returns401() {
        final ResponseEntity<Void> response = restTemplate
                .withBasicAuth("unknownuser", TestFactory.DEFAULT_PASSWORD)
                .getForEntity(baseUrl(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── FILTER TESTS ──────────────────────────────────────────────────────

    @Test
    void getAllAssignedComponents_filterByStatus_returnsMatchingOnly() {
        saveAssignment(saveComponent(), AssignmentStatus.BOOKED);
        saveAssignment(saveComponent("Agile Video", ComponentType.MEDIA),
                AssignmentStatus.COMPLETED);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "?status=BOOKED",
                        TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(1);
        assertThat(response.getBody().content.get(0).getUserStatus())
                .isEqualTo(AssignmentStatus.BOOKED);
    }

    @Test
    void getAllAssignedComponents_filterByType_returnsMatchingOnly() {
        saveAssignment(saveComponent());
        saveAssignment(saveComponent("Agile Video", ComponentType.MEDIA));

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "?type=COURSE",
                        TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(1);
        assertThat(response.getBody().content.get(0).getType())
                .isEqualTo(ComponentType.COURSE);
    }

    @Test
    void getAllAssignedComponents_filterByCategory_returnsMatchingOnly() {
        saveAssignment(saveComponent());

        final ELearningComponent leadershipComponent =
                buildComponent("Leadership 101", ComponentType.COURSE);
        leadershipComponent.setCategory(ComponentCategory.LEADERSHIP);
        final ELearningComponent saved =
                componentRepository.save(leadershipComponent);
        saveAssignment(saved);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "?category=SOFTWARE_DEVELOPMENT",
                        TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(1);
    }

    @Test
    void getAllAssignedComponents_filterByStatusAndType_returnsMatchingOnly() {
        saveAssignment(saveComponent(), AssignmentStatus.BOOKED);
        saveAssignment(saveComponent("Agile Video", ComponentType.MEDIA),
                AssignmentStatus.BOOKED);
        saveAssignment(saveComponent("Java Basics", ComponentType.COURSE),
                AssignmentStatus.COMPLETED);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "?status=BOOKED&type=COURSE",
                        TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).hasSize(1);
        assertThat(response.getBody().content.get(0).getUserStatus())
                .isEqualTo(AssignmentStatus.BOOKED);
        assertThat(response.getBody().content.get(0).getType())
                .isEqualTo(ComponentType.COURSE);
    }

    @Test
    void getAllAssignedComponents_invalidStatusFilter_returns400() {
        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .getForEntity(
                        baseUrl() + "?status=INVALID_STATUS",
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllAssignedComponents_invalidTypeFilter_returns400() {
        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .getForEntity(
                        baseUrl() + "?type=INVALID_TYPE",
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllAssignedComponents_filterReturnsEmpty_returns200WithEmptyPage() {
        saveAssignment(saveComponent(), AssignmentStatus.BOOKED);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "?status=COMPLETED",
                        TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content).isEmpty();
        assertThat(response.getBody().totalElements).isEqualTo(0);
    }

    // ─── GET COMPONENT DETAIL ──────────────────────────────────────────────

    @Test
    void getComponentDetail_authenticatedUser_returns200() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<ComponentDetailResponse> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "/" + component.getId(),
                        ComponentDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(component.getId());
        assertThat(response.getBody().getName())
                .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
    }

    @Test
    void getComponentDetail_correctFieldsReturned() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component, AssignmentStatus.IN_PROGRESS);

        final ResponseEntity<ComponentDetailResponse> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "/" + component.getId(),
                        ComponentDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName())
                .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
        assertThat(response.getBody().getType())
                .isEqualTo(TestFactory.DEFAULT_TYPE);
        assertThat(response.getBody().getUserStatus())
                .isEqualTo(AssignmentStatus.IN_PROGRESS);
        assertThat(response.getBody().getDuration())
                .isEqualTo("8 hours");
        assertThat(response.getBody().getCategory())
                .isEqualTo(TestFactory.DEFAULT_CATEGORY);
    }

    @Test
    void getComponentDetail_durationFormattedCorrectly() {
        final ELearningComponent component = saveComponent();
        component.setDurationInMinutes(90);
        componentRepository.save(component);
        saveAssignment(component);

        final ResponseEntity<ComponentDetailResponse> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "/" + component.getId(),
                        ComponentDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDuration()).isEqualTo("1h 30m");
    }

    @Test
    void getComponentDetail_availableDatesReturned() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<ComponentDetailResponse> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl() + "/" + component.getId(),
                        ComponentDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAvailableDates()).isNotNull();
        assertThat(response.getBody().getAvailableDates().getStartDate())
                .isEqualTo("2024-01-01");
        assertThat(response.getBody().getAvailableDates().getEndDate())
                .isEqualTo("2024-12-31");
    }

    @Test
    void getComponentDetail_componentNotAssignedToUser_returns404() {
        final ELearningComponent component = saveComponent();

        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .getForEntity(
                        baseUrl() + "/" + component.getId(),
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getComponentDetail_nonExistentComponentId_returns404() {
        final ResponseEntity<Void> response = authenticatedRestTemplate()
                .getForEntity(
                        baseUrl() + "/" + java.util.UUID.randomUUID(),
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getComponentDetail_noAuthentication_returns401() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<Void> response = restTemplate
                .getForEntity(
                        baseUrl() + "/" + component.getId(),
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getComponentDetail_wrongPassword_returns401() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<Void> response = restTemplate
                .withBasicAuth(TestFactory.DEFAULT_USERNAME, "wrongpassword")
                .getForEntity(
                        baseUrl() + "/" + component.getId(),
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getComponentDetail_wrongUsername_returns401() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<Void> response = restTemplate
                .withBasicAuth("unknownuser", TestFactory.DEFAULT_PASSWORD)
                .getForEntity(
                        baseUrl() + "/" + component.getId(),
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getComponentDetail_otherUserCannotAccessMyComponent() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("OtherPass!"));
        otherUser.setEmail("other@example.com");
        otherUser.setRole(UserRole.ROLE_USER);
        userRepository.save(otherUser);

        final ResponseEntity<Void> response = restTemplate
                .withBasicAuth("otheruser", "OtherPass!")
                .getForEntity(
                        baseUrl() + "/" + component.getId(),
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

