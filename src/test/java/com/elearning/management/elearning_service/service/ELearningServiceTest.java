package com.elearning.management.elearning_service.service;

import com.elearning.management.elearning_service.TestFactory;
import com.elearning.management.elearning_service.domain.*;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.exception.UserNotFoundException;
import com.elearning.management.elearning_service.repository.ELearningComponentRepository;
import com.elearning.management.elearning_service.repository.UserAssignmentRepository;
import com.elearning.management.elearning_service.repository.UserRepository;
import com.elearning.management.elearning_service.transform.ELearningMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.elearning.management.elearning_service.TestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ELearningServiceTest {

    @Mock
    private ELearningComponentRepository componentRepository;

    @Mock
    private UserAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ELearningMapper eLearningMapper;

    @InjectMocks
    private ELearningService eLearningService;

    // ─── GET ALL ASSIGNED COMPONENTS ───────────────────────────────────────

    @Test
    void getAllAssignedComponents_validUser_returnsPagedResponse() {
        final User user = buildUser("encodedPassword");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<UserAssignment> assignmentPage = new PageImpl<>(
                List.of(assignment), pageable, 1);
        final AssignedComponentResponse expectedResponse =
                new AssignedComponentResponse();

        when(userRepository.findByUsername(DEFAULT_USERNAME))
                .thenReturn(Optional.of(user));
        when(assignmentRepository.findByUserWithComponentAndTags(user, pageable))
                .thenReturn(assignmentPage);
        when(eLearningMapper.toAssignedComponentResponse(assignment))
                .thenReturn(expectedResponse);

        final Page<AssignedComponentResponse> result =
                eLearningService.getAllAssignedComponents(DEFAULT_USERNAME, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(expectedResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findByUsername(DEFAULT_USERNAME);
        verify(assignmentRepository).findByUserWithComponentAndTags(user, pageable);
    }

    @Test
    void getAllAssignedComponents_userHasNoAssignments_returnsEmptyPage() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<UserAssignment> emptyPage = new PageImpl<>(
                List.of(), pageable, 0);

        when(userRepository.findByUsername(DEFAULT_USERNAME))
                .thenReturn(Optional.of(user));
        when(assignmentRepository.findByUserWithComponentAndTags(user, pageable))
                .thenReturn(emptyPage);

        final Page<AssignedComponentResponse> result =
                eLearningService.getAllAssignedComponents(DEFAULT_USERNAME, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(eLearningMapper, never()).toAssignedComponentResponse(any());
    }

    @Test
    void getAllAssignedComponents_multipleAssignments_returnsAllMapped() {
        final User user = buildUser("encodedPassword");
        final ELearningComponent component1 = buildComponent();
        final ELearningComponent component2 = buildComponent("Agile Video",
                ComponentType.MEDIA);
        final UserAssignment assignment1 = buildAssignment(user, component1);
        final UserAssignment assignment2 = buildAssignment(user, component2);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<UserAssignment> assignmentPage = new PageImpl<>(
                List.of(assignment1, assignment2), pageable, 2);
        final AssignedComponentResponse response1 = new AssignedComponentResponse();
        final AssignedComponentResponse response2 = new AssignedComponentResponse();

        when(userRepository.findByUsername(DEFAULT_USERNAME))
                .thenReturn(Optional.of(user));
        when(assignmentRepository.findByUserWithComponentAndTags(user, pageable))
                .thenReturn(assignmentPage);
        when(eLearningMapper.toAssignedComponentResponse(assignment1))
                .thenReturn(response1);
        when(eLearningMapper.toAssignedComponentResponse(assignment2))
                .thenReturn(response2);

        final Page<AssignedComponentResponse> result =
                eLearningService.getAllAssignedComponents(DEFAULT_USERNAME, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(response1, response2);
        verify(eLearningMapper, times(2)).toAssignedComponentResponse(any());
    }

    @Test
    void getAllAssignedComponents_userNotFound_throwsUserNotFoundException() {
        final Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findByUsername(DEFAULT_USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                eLearningService.getAllAssignedComponents(DEFAULT_USERNAME, pageable))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(DEFAULT_USERNAME);

        verify(assignmentRepository, never())
                .findByUserWithComponentAndTags(any(), any());
        verify(eLearningMapper, never())
                .toAssignedComponentResponse(any());
    }

    @Test
    void getAllAssignedComponents_respectsPageSize() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 5);
        final Page<UserAssignment> assignmentPage = new PageImpl<>(
                List.of(), pageable, 0);

        when(userRepository.findByUsername(DEFAULT_USERNAME))
                .thenReturn(Optional.of(user));
        when(assignmentRepository.findByUserWithComponentAndTags(user, pageable))
                .thenReturn(assignmentPage);

        eLearningService.getAllAssignedComponents(DEFAULT_USERNAME, pageable);

        verify(assignmentRepository)
                .findByUserWithComponentAndTags(eq(user), eq(pageable));
    }
}

