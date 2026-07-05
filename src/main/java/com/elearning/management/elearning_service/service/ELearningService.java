package com.elearning.management.elearning_service.service;

import com.elearning.management.elearning_service.config.CacheConfig;
import com.elearning.management.elearning_service.domain.ELearningComponent;
import com.elearning.management.elearning_service.domain.User;
import com.elearning.management.elearning_service.domain.UserAssignment;
import com.elearning.management.elearning_service.dto.projection.AssignedComponentProjection;
import com.elearning.management.elearning_service.dto.request.ComponentFilter;
import com.elearning.management.elearning_service.dto.request.CreateComponentRequest;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.dto.response.CacheablePage;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import com.elearning.management.elearning_service.dto.response.CreateComponentResponse;
import com.elearning.management.elearning_service.exception.AssignmentNotFoundException;
import com.elearning.management.elearning_service.exception.DataPersistenceException;
import com.elearning.management.elearning_service.repository.ELearningComponentRepository;
import com.elearning.management.elearning_service.repository.UserAssignmentRepository;
import com.elearning.management.elearning_service.transform.ELearningMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ELearningService {

    private final UserAssignmentRepository assignmentRepository;
    private final ELearningComponentRepository componentRepository;
    private final ELearningMapper eLearningMapper;

    public ELearningService(
            final UserAssignmentRepository assignmentRepository,
            final ELearningComponentRepository componentRepository,
            final ELearningMapper eLearningMapper) {
        this.assignmentRepository = assignmentRepository;
        this.componentRepository = componentRepository;
        this.eLearningMapper = eLearningMapper;
    }

    // ─── Public methods ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.USER_ASSIGNMENTS_CACHE,
            key = "#user.id + '_' + #filter.status + '_' + #filter.type" +
                    " + '_' + #filter.category + '_' + #pageable.pageNumber" +
                    " + '_' + #pageable.pageSize"
    )
    public CacheablePage<AssignedComponentResponse> getAllAssignedComponents(
            final User user,
            final ComponentFilter filter,
            final Pageable pageable) {

        final Page<AssignedComponentResponse> page =
                fetchAssignedComponentProjections(user, filter, pageable)
                        .map(eLearningMapper::toAssignedComponentResponse);

        return new CacheablePage<>(
                page.getContent(),
                pageable,
                page.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.COMPONENT_DETAIL_CACHE,
            key = "#user.id + '_' + #componentId"
    )
    public ComponentDetailResponse getComponentDetail(
            final UUID componentId,
            final User user) {

        final UserAssignment assignment =
                fetchAssignmentWithDetails(user, componentId);

        return eLearningMapper.toComponentDetailResponse(
                assignment.getComponent(), assignment);
    }

    @Transactional
    public CreateComponentResponse createComponent(final CreateComponentRequest request) {
        final ELearningComponent component = new ELearningComponent();
        component.setName(request.name());
        component.setDescription(request.description());
        component.setType(request.type());
        component.setImageUrl(request.imageUrl());
        component.setDurationInMinutes(request.durationInMinutes());
        component.setCategory(request.category());
        component.setAvailableStartDate(request.availableStartDate());
        component.setAvailableEndDate(request.availableEndDate());

        try {
            final ELearningComponent saved = componentRepository.save(component);
            return new CreateComponentResponse(
                    saved.getId(),
                    saved.getName(),
                    saved.getType(),
                    saved.getCategory(),
                    saved.getDateCreated());
        } catch (DataAccessException ex) {
            throw new DataPersistenceException(
                    "Failed to create component due to a database error");
        }
    }

    // ─── Private DB helpers ────────────────────────────────────────────────

    private Page<AssignedComponentProjection> fetchAssignedComponentProjections(
            final User user,
            final ComponentFilter filter,
            final Pageable pageable) {
        try {
            return assignmentRepository.findAssignedComponentProjections(
                    user,
                    filter.status(),
                    filter.type(),
                    filter.category(),
                    pageable);
        } catch (DataAccessException ex) {
            throw new DataPersistenceException(
                    "Failed to retrieve assigned components due to a database error");
        }
    }

    private UserAssignment fetchAssignmentWithDetails(
            final User user,
            final UUID componentId) {
        try {
            return assignmentRepository
                    .findByUserAndComponentIdWithDetails(user, componentId)
                    .orElseThrow(() -> new AssignmentNotFoundException(componentId));
        } catch (AssignmentNotFoundException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new DataPersistenceException(
                    "Failed to retrieve component details due to a database error");
        }
    }

    // ─── Cache eviction ────────────────────────────────────────────────────
    // When update/delete endpoints are implemented, add @CacheEvict methods
    // here to evict USER_ASSIGNMENTS_CACHE and COMPONENT_DETAIL_CACHE
    // accordingly to prevent stale data being served after mutations.
}

