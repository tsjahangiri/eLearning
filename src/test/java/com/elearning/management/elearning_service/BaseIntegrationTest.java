package com.elearning.management.elearning_service;

import com.elearning.management.elearning_service.domain.ELearningComponent;
import com.elearning.management.elearning_service.domain.User;
import com.elearning.management.elearning_service.domain.UserAssignment;
import com.elearning.management.elearning_service.repository.ELearningComponentRepository;
import com.elearning.management.elearning_service.repository.UserAssignmentRepository;
import com.elearning.management.elearning_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ELearningComponentRepository componentRepository;

    @Autowired
    protected UserAssignmentRepository assignmentRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @LocalServerPort
    protected int port;

    protected User testUser;
    protected String encodedPassword;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        componentRepository.deleteAll();
        userRepository.deleteAll();

        encodedPassword = passwordEncoder.encode(TestFactory.DEFAULT_PASSWORD);
        testUser = userRepository.save(TestFactory.buildUser(encodedPassword));
    }

    protected TestRestTemplate authenticatedRestTemplate() {
        return restTemplate.withBasicAuth(
                TestFactory.DEFAULT_USERNAME,
                TestFactory.DEFAULT_PASSWORD);
    }

    protected ELearningComponent saveComponent() {
        return componentRepository.save(TestFactory.buildComponent());
    }

    protected ELearningComponent saveComponent(final String name,
                                               final com.elearning.management.elearning_service.domain.ComponentType type) {
        return componentRepository.save(TestFactory.buildComponent(name, type));
    }

    protected UserAssignment saveAssignment(final ELearningComponent component) {
        return assignmentRepository.save(
                TestFactory.buildAssignment(testUser, component));
    }

    protected UserAssignment saveAssignment(final ELearningComponent component,
                                            final com.elearning.management.elearning_service.domain.AssignmentStatus status) {
        return assignmentRepository.save(
                TestFactory.buildAssignment(testUser, component, status));
    }

    protected String baseUrl() {
        return "http://localhost:" + port + "/api/v1/lms/elearning-components";
    }
}

