package com.elearning.management.elearning_service.controller;

import com.elearning.management.elearning_service.BaseIntegrationTest;
import com.elearning.management.elearning_service.TestFactory;
import com.elearning.management.elearning_service.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ELearningControllerIT extends BaseIntegrationTest {

    // ─── POSITIVE TESTS ────────────────────────────────────────────────────

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
        final ELearningComponent component1 = saveComponent();
        final ELearningComponent component2 = saveComponent(
                "Agile Video", ComponentType.MEDIA);
        saveAssignment(component1);
        saveAssignment(component2);

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
        final ELearningComponent component = saveComponent();
        saveAssignment(component, AssignmentStatus.IN_PROGRESS);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content.get(0).getUserStatus())
                .isEqualTo(AssignmentStatus.IN_PROGRESS);
    }

    @Test
    void getAllAssignedComponents_correctComponentNameReturned() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        final ResponseEntity<TestFactory.AssignedComponentPage> response =
                authenticatedRestTemplate().getForEntity(
                        baseUrl(), TestFactory.AssignedComponentPage.class);

        assertThat(response.getBody().content.get(0).getName())
                .isEqualTo(TestFactory.DEFAULT_COMPONENT_NAME);
    }

    // ─── NEGATIVE TESTS ────────────────────────────────────────────────────

    @Test
    void getAllAssignedComponents_noAuthentication_returns401() {
        final ResponseEntity<Void> response = restTemplate.getForEntity(
                baseUrl(), Void.class);

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

    @Test
    void getAllAssignedComponents_onlyReturnsCurrentUserAssignments() {
        final ELearningComponent component = saveComponent();
        saveAssignment(component);

        // Create second user with their own assignment
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
}

