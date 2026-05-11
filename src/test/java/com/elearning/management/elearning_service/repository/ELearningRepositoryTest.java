package com.elearning.management.elearning_service.repository;

import com.elearning.management.elearning_service.config.DomainConfig;
import com.elearning.management.elearning_service.domain.*;
import com.elearning.management.elearning_service.dto.projection.AssignedComponentProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static com.elearning.management.elearning_service.TestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(DomainConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ELearningRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ELearningComponentRepository componentRepository;

    @Autowired
    private UserAssignmentRepository assignmentRepository;

    private User savedUser;
    private ELearningComponent savedComponent;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        componentRepository.deleteAll();
        userRepository.deleteAll();

        savedUser = userRepository.save(buildUser("encodedPassword"));
        savedComponent = componentRepository.save(buildComponent());
    }

    // ─── UserRepository ────────────────────────────────────────────────────

    @Test
    void findByUsername_existingUser_returnsUser() {
        final Optional<User> result =
                userRepository.findByUsername(DEFAULT_USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(result.get().getEmail())
                .isEqualTo("testuser@example.com");
    }

    @Test
    void findByUsername_nonExistentUser_returnsEmpty() {
        final Optional<User> result =
                userRepository.findByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUsername_existingUser_returnsTrue() {
        assertThat(userRepository
                .existsByUsername(DEFAULT_USERNAME)).isTrue();
    }

    @Test
    void existsByUsername_nonExistentUser_returnsFalse() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        assertThat(userRepository
                .existsByEmail("testuser@example.com")).isTrue();
    }

    // ─── ELearningComponentRepository ─────────────────────────────────────

    @Test
    void findById_existingComponent_returnsComponent() {
        final Optional<ELearningComponent> result =
                componentRepository.findById(savedComponent.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(result.get().getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    void findById_nonExistentComponent_returnsEmpty() {
        final Optional<ELearningComponent> result =
                componentRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByType_returnsOnlyMatchingType() {
        componentRepository.save(
                buildComponent("Agile Video", ComponentType.MEDIA));
        componentRepository.save(
                buildComponent("Java Basics", ComponentType.COURSE));

        final Page<ELearningComponent> result = componentRepository
                .findByType(ComponentType.COURSE, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(c -> c.getType() == ComponentType.COURSE);
    }

    @Test
    void findByCategory_returnsOnlyMatchingCategory() {
        final ELearningComponent otherCategory = buildComponent();
        otherCategory.setCategory(ComponentCategory.LEADERSHIP);
        componentRepository.save(otherCategory);

        final Page<ELearningComponent> result = componentRepository
                .findByCategory(DEFAULT_CATEGORY, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory())
                .isEqualTo(DEFAULT_CATEGORY);
    }

    // ─── UserAssignmentRepository ──────────────────────────────────────────

    @Test
    void findByUserAndComponent_existingAssignment_returnsAssignment() {
        assignmentRepository.save(
                buildAssignment(savedUser, savedComponent));

        final Optional<UserAssignment> result =
                assignmentRepository.findByUserAndComponent(
                        savedUser, savedComponent);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void findByUserAndComponent_noAssignment_returnsEmpty() {
        final Optional<UserAssignment> result =
                assignmentRepository.findByUserAndComponent(
                        savedUser, savedComponent);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUserAndComponent_existingAssignment_returnsTrue() {
        assignmentRepository.save(
                buildAssignment(savedUser, savedComponent));

        assertThat(assignmentRepository
                .existsByUserAndComponent(
                        savedUser, savedComponent)).isTrue();
    }

    @Test
    void existsByUserAndComponent_noAssignment_returnsFalse() {
        assertThat(assignmentRepository
                .existsByUserAndComponent(
                        savedUser, savedComponent)).isFalse();
    }

    @Test
    void findAssignedComponentProjections_returnsProjections() {
        assignmentRepository.save(
                buildAssignment(savedUser, savedComponent));

        final Page<AssignedComponentProjection> result =
                assignmentRepository.findAssignedComponentProjections(
                        savedUser, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).componentName())
                .isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(result.getContent().get(0).componentType())
                .isEqualTo(DEFAULT_TYPE);
        assertThat(result.getContent().get(0).assignmentStatus())
                .isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void findAssignedComponentProjections_emptyAssignments_returnsEmptyPage() {
        final Page<AssignedComponentProjection> result =
                assignmentRepository.findAssignedComponentProjections(
                        savedUser, PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findAssignedComponentProjections_onlyReturnsCurrentUserData() {
        final User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("encoded");
        otherUser.setEmail("other@example.com");
        otherUser.setRole(UserRole.ROLE_USER);
        final User savedOtherUser = userRepository.save(otherUser);

        final ELearningComponent otherComponent = componentRepository
                .save(buildComponent("Other Course", ComponentType.MEDIA));

        assignmentRepository.save(
                buildAssignment(savedUser, savedComponent));
        assignmentRepository.save(
                buildAssignment(savedOtherUser, otherComponent));

        final Page<AssignedComponentProjection> result =
                assignmentRepository.findAssignedComponentProjections(
                        savedUser, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).componentName())
                .isEqualTo(DEFAULT_COMPONENT_NAME);
    }

    @Test
    void findByUserAndComponentIdWithDetails_existingAssignment_returnsWithComponent() {
        assignmentRepository.save(
                buildAssignment(savedUser, savedComponent));

        final Optional<UserAssignment> result =
                assignmentRepository.findByUserAndComponentIdWithDetails(
                        savedUser, savedComponent.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getComponent()).isNotNull();
        assertThat(result.get().getComponent().getName())
                .isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(result.get().getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void findByUserAndComponentIdWithDetails_notAssigned_returnsEmpty() {
        final Optional<UserAssignment> result =
                assignmentRepository.findByUserAndComponentIdWithDetails(
                        savedUser, savedComponent.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserAndComponentIdWithDetails_nonExistentComponent_returnsEmpty() {
        final Optional<UserAssignment> result =
                assignmentRepository.findByUserAndComponentIdWithDetails(
                        savedUser, UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserAndStatus_returnsOnlyMatchingStatus() {
        final ELearningComponent component2 = componentRepository
                .save(buildComponent("Agile Video", ComponentType.MEDIA));

        assignmentRepository.save(
                buildAssignment(savedUser, savedComponent,
                        AssignmentStatus.BOOKED));
        assignmentRepository.save(
                buildAssignment(savedUser, component2,
                        AssignmentStatus.COMPLETED));

        final Page<UserAssignment> result =
                assignmentRepository.findByUserAndStatus(
                        savedUser, AssignmentStatus.BOOKED,
                        PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus())
                .isEqualTo(AssignmentStatus.BOOKED);
    }
}

