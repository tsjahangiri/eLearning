package com.elearning.management.elearning_service.repository;

import com.elearning.management.elearning_service.domain.AssignmentStatus;
import com.elearning.management.elearning_service.domain.ELearningComponent;
import com.elearning.management.elearning_service.domain.User;
import com.elearning.management.elearning_service.domain.UserAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAssignmentRepository extends JpaRepository<UserAssignment, UUID> {

    Page<UserAssignment> findByUser(User user, Pageable pageable);

    Optional<UserAssignment> findByUserAndComponent(User user, ELearningComponent component);

    Page<UserAssignment> findByUserAndStatus(User user, AssignmentStatus status, Pageable pageable);

    boolean existsByUserAndComponent(User user, ELearningComponent component);

}

