package com.elearning.management.elearning_service.repository;

import com.elearning.management.elearning_service.domain.AssignmentStatus;
import com.elearning.management.elearning_service.domain.ELearningComponent;
import com.elearning.management.elearning_service.domain.User;
import com.elearning.management.elearning_service.domain.UserAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAssignmentRepository extends JpaRepository<UserAssignment, UUID> {

    Page<UserAssignment> findByUser(User user, Pageable pageable);

    Optional<UserAssignment> findByUserAndComponent(User user, ELearningComponent component);

    Page<UserAssignment> findByUserAndStatus(User user, AssignmentStatus status, Pageable pageable);

    boolean existsByUserAndComponent(User user, ELearningComponent component);

    @Query("""
            SELECT ua FROM UserAssignment ua
            JOIN FETCH ua.component c
            LEFT JOIN FETCH c.metaTags
            WHERE ua.user = :user
            """)
    Page<UserAssignment> findByUserWithComponentAndTags(
            @Param("user") User user,
            Pageable pageable);
}

