package com.elearning.management.elearning_service.service;

import com.elearning.management.elearning_service.domain.*;
import com.elearning.management.elearning_service.dto.projection.AssignedComponentProjection;
import com.elearning.management.elearning_service.dto.request.ComponentFilter;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.dto.response.CacheablePage;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import com.elearning.management.elearning_service.exception.AssignmentNotFoundException;
import com.elearning.management.elearning_service.exception.DataPersistenceException;
import com.elearning.management.elearning_service.repository.UserAssignmentRepository;
import com.elearning.management.elearning_service.transform.ELearningMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.elearning.management.elearning_service.TestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ELearningServiceTest {

    @Mock
    private UserAssignmentRepository assignmentRepository;

    @Mock
    private ELearningMapper eLearningMapper;

    @InjectMocks
    private ELearningService eLearningService;

    // ─── GET ALL ASSIGNED COMPONENTS ───────────────────────────────────────

    @Test
    void getAllAssignedComponents_noFilters_returnsPagedResponse() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(null, null, null);
        final AssignedComponentProjection projection = buildProjection();
        final Page<AssignedComponentProjection> projectionPage =
                new PageImpl<>(List.of(projection), pageable, 1);
        final AssignedComponentResponse expectedResponse =
                new AssignedComponentResponse();

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null, null, pageable))
                .thenReturn(projectionPage);
        when(eLearningMapper.toAssignedComponentResponse(projection))
                .thenReturn(expectedResponse);

        final CacheablePage<AssignedComponentResponse> result =
                eLearningService.getAllAssignedComponents(user, filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(expectedResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(assignmentRepository).findAssignedComponentProjections(
                user, null, null, null, pageable);
    }

    @Test
    void getAllAssignedComponents_withStatusFilter_passesStatusToRepository() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter =
                new ComponentFilter(AssignmentStatus.BOOKED, null, null);
        final Page<AssignedComponentProjection> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(assignmentRepository.findAssignedComponentProjections(
                user, AssignmentStatus.BOOKED, null, null, pageable))
                .thenReturn(emptyPage);

        eLearningService.getAllAssignedComponents(user, filter, pageable);

        verify(assignmentRepository).findAssignedComponentProjections(
                user, AssignmentStatus.BOOKED, null, null, pageable);
    }

    @Test
    void getAllAssignedComponents_withTypeFilter_passesTypeToRepository() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter =
                new ComponentFilter(null, ComponentType.COURSE, null);
        final Page<AssignedComponentProjection> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, ComponentType.COURSE, null, pageable))
                .thenReturn(emptyPage);

        eLearningService.getAllAssignedComponents(user, filter, pageable);

        verify(assignmentRepository).findAssignedComponentProjections(
                user, null, ComponentType.COURSE, null, pageable);
    }

    @Test
    void getAllAssignedComponents_withCategoryFilter_passesCategoryToRepository() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(
                null, null, ComponentCategory.SOFTWARE_DEVELOPMENT);
        final Page<AssignedComponentProjection> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null,
                ComponentCategory.SOFTWARE_DEVELOPMENT, pageable))
                .thenReturn(emptyPage);

        eLearningService.getAllAssignedComponents(user, filter, pageable);

        verify(assignmentRepository).findAssignedComponentProjections(
                user, null, null,
                ComponentCategory.SOFTWARE_DEVELOPMENT, pageable);
    }

    @Test
    void getAllAssignedComponents_withAllFilters_passesAllToRepository() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(
                AssignmentStatus.BOOKED,
                ComponentType.COURSE,
                ComponentCategory.SOFTWARE_DEVELOPMENT);
        final Page<AssignedComponentProjection> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(assignmentRepository.findAssignedComponentProjections(
                user,
                AssignmentStatus.BOOKED,
                ComponentType.COURSE,
                ComponentCategory.SOFTWARE_DEVELOPMENT,
                pageable))
                .thenReturn(emptyPage);

        eLearningService.getAllAssignedComponents(user, filter, pageable);

        verify(assignmentRepository).findAssignedComponentProjections(
                user,
                AssignmentStatus.BOOKED,
                ComponentType.COURSE,
                ComponentCategory.SOFTWARE_DEVELOPMENT,
                pageable);
    }

    @Test
    void getAllAssignedComponents_userHasNoAssignments_returnsEmptyPage() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(null, null, null);
        final Page<AssignedComponentProjection> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null, null, pageable))
                .thenReturn(emptyPage);

        final CacheablePage<AssignedComponentResponse> result =
                eLearningService.getAllAssignedComponents(user, filter, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(eLearningMapper, never()).toAssignedComponentResponse(any());
    }

    @Test
    void getAllAssignedComponents_multipleAssignments_returnsAllMapped() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(null, null, null);
        final AssignedComponentProjection projection1 = buildProjection();
        final AssignedComponentProjection projection2 = buildProjection();
        final Page<AssignedComponentProjection> projectionPage =
                new PageImpl<>(List.of(projection1, projection2), pageable, 2);
        final AssignedComponentResponse response1 = new AssignedComponentResponse();
        final AssignedComponentResponse response2 = new AssignedComponentResponse();

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null, null, pageable))
                .thenReturn(projectionPage);
        when(eLearningMapper.toAssignedComponentResponse(projection1))
                .thenReturn(response1);
        when(eLearningMapper.toAssignedComponentResponse(projection2))
                .thenReturn(response2);

        final CacheablePage<AssignedComponentResponse> result =
                eLearningService.getAllAssignedComponents(user, filter, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(response1, response2);
        verify(eLearningMapper, times(2)).toAssignedComponentResponse(any());
    }

    @Test
    void getAllAssignedComponents_passesCorrectPageableToRepository() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(2, 5);
        final ComponentFilter filter = new ComponentFilter(null, null, null);
        final Page<AssignedComponentProjection> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null, null, pageable))
                .thenReturn(emptyPage);

        eLearningService.getAllAssignedComponents(user, filter, pageable);

        verify(assignmentRepository).findAssignedComponentProjections(
                eq(user), eq(null), eq(null), eq(null), eq(pageable));
    }

    @Test
    void getAllAssignedComponents_databaseError_throwsDataPersistenceException() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(null, null, null);

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null, null, pageable))
                .thenThrow(new QueryTimeoutException("query timed out"));

        assertThatThrownBy(() ->
                eLearningService.getAllAssignedComponents(user, filter, pageable))
                .isInstanceOf(DataPersistenceException.class)
                .hasMessageContaining("Failed to retrieve assigned components");

        verify(eLearningMapper, never()).toAssignedComponentResponse(any());
    }

    @Test
    void getAllAssignedComponents_connectionLost_throwsDataPersistenceException() {
        final User user = buildUser("encodedPassword");
        final Pageable pageable = PageRequest.of(0, 20);
        final ComponentFilter filter = new ComponentFilter(null, null, null);

        when(assignmentRepository.findAssignedComponentProjections(
                user, null, null, null, pageable))
                .thenThrow(new DataAccessException("connection lost") {});

        assertThatThrownBy(() ->
                eLearningService.getAllAssignedComponents(user, filter, pageable))
                .isInstanceOf(DataPersistenceException.class)
                .hasMessageContaining("Failed to retrieve assigned components");
    }

    // ─── GET COMPONENT DETAIL ──────────────────────────────────────────────

    @Test
    void getComponentDetail_validUserAndComponent_returnsResponse() {
        final UUID componentId = UUID.randomUUID();
        final User user = buildUser("encodedPassword");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);
        final ComponentDetailResponse expectedResponse =
                new ComponentDetailResponse();

        when(assignmentRepository
                .findByUserAndComponentIdWithDetails(user, componentId))
                .thenReturn(Optional.of(assignment));
        when(eLearningMapper.toComponentDetailResponse(component, assignment))
                .thenReturn(expectedResponse);

        final ComponentDetailResponse result =
                eLearningService.getComponentDetail(componentId, user);

        assertThat(result).isEqualTo(expectedResponse);
        verify(assignmentRepository)
                .findByUserAndComponentIdWithDetails(user, componentId);
        verify(eLearningMapper)
                .toComponentDetailResponse(component, assignment);
    }

    @Test
    void getComponentDetail_componentNotAssignedToUser_throwsAssignmentNotFoundException() {
        final UUID componentId = UUID.randomUUID();
        final User user = buildUser("encodedPassword");

        when(assignmentRepository
                .findByUserAndComponentIdWithDetails(user, componentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                eLearningService.getComponentDetail(componentId, user))
                .isInstanceOf(AssignmentNotFoundException.class)
                .hasMessageContaining(componentId.toString());

        verify(eLearningMapper, never())
                .toComponentDetailResponse(any(), any());
    }

    @Test
    void getComponentDetail_nonExistentComponent_throwsAssignmentNotFoundException() {
        final UUID componentId = UUID.randomUUID();
        final User user = buildUser("encodedPassword");

        when(assignmentRepository
                .findByUserAndComponentIdWithDetails(user, componentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                eLearningService.getComponentDetail(componentId, user))
                .isInstanceOf(AssignmentNotFoundException.class)
                .hasMessageContaining(componentId.toString());
    }

    @Test
    void getComponentDetail_databaseError_throwsDataPersistenceException() {
        final UUID componentId = UUID.randomUUID();
        final User user = buildUser("encodedPassword");

        when(assignmentRepository
                .findByUserAndComponentIdWithDetails(user, componentId))
                .thenThrow(new QueryTimeoutException("query timed out"));

        assertThatThrownBy(() ->
                eLearningService.getComponentDetail(componentId, user))
                .isInstanceOf(DataPersistenceException.class)
                .hasMessageContaining("Failed to retrieve component details");

        verify(eLearningMapper, never())
                .toComponentDetailResponse(any(), any());
    }

    @Test
    void getComponentDetail_connectionLost_throwsDataPersistenceException() {
        final UUID componentId = UUID.randomUUID();
        final User user = buildUser("encodedPassword");

        when(assignmentRepository
                .findByUserAndComponentIdWithDetails(user, componentId))
                .thenThrow(new DataAccessException("connection lost") {});

        assertThatThrownBy(() ->
                eLearningService.getComponentDetail(componentId, user))
                .isInstanceOf(DataPersistenceException.class)
                .hasMessageContaining("Failed to retrieve component details");
    }

    @Test
    void getComponentDetail_mapperCalledWithCorrectArguments() {
        final UUID componentId = UUID.randomUUID();
        final User user = buildUser("encodedPassword");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);

        when(assignmentRepository
                .findByUserAndComponentIdWithDetails(user, componentId))
                .thenReturn(Optional.of(assignment));
        when(eLearningMapper.toComponentDetailResponse(component, assignment))
                .thenReturn(new ComponentDetailResponse());

        eLearningService.getComponentDetail(componentId, user);

        verify(eLearningMapper)
                .toComponentDetailResponse(component, assignment);
    }
}

