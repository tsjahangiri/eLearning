package com.elearning.management.elearning_service.service;

import com.elearning.management.elearning_service.domain.ELearningComponent;
import com.elearning.management.elearning_service.domain.User;
import com.elearning.management.elearning_service.domain.UserAssignment;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import com.elearning.management.elearning_service.exception.AssignmentNotFoundException;
import com.elearning.management.elearning_service.exception.ComponentNotFoundException;
import com.elearning.management.elearning_service.repository.ELearningComponentRepository;
import com.elearning.management.elearning_service.repository.UserAssignmentRepository;
import com.elearning.management.elearning_service.repository.UserRepository;
import com.elearning.management.elearning_service.transform.ELearningMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ELearningService {

    private final ELearningComponentRepository componentRepository;
    private final UserAssignmentRepository assignmentRepository;
    private final ELearningMapper eLearningMapper;

    public ELearningService(
            final ELearningComponentRepository componentRepository,
            final UserAssignmentRepository assignmentRepository,
            final UserRepository userRepository,
            final ELearningMapper eLearningMapper) {
        this.componentRepository = componentRepository;
        this.assignmentRepository = assignmentRepository;
        this.eLearningMapper = eLearningMapper;
    }

    @Transactional(readOnly = true)
    public ComponentDetailResponse getComponentDetail(
            final UUID componentId,
            final User user) {

        final ELearningComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new ComponentNotFoundException(componentId));

        final UserAssignment assignment = assignmentRepository
                .findByUserAndComponent(user, component)
                .orElseThrow(() -> new AssignmentNotFoundException(componentId));

        return eLearningMapper.toComponentDetailResponse(component, assignment);
    }

    @Transactional(readOnly = true)
    public Page<AssignedComponentResponse> getAllAssignedComponents(
            final User user,
            final Pageable pageable) {

        return assignmentRepository
                .findByUserWithComponentAndTags(user, pageable)
                .map(eLearningMapper::toAssignedComponentResponse);
    }
}

