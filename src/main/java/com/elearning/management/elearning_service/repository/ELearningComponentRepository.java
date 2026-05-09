package com.elearning.management.elearning_service.repository;

import com.elearning.management.elearning_service.domain.ComponentCategory;
import com.elearning.management.elearning_service.domain.ComponentType;
import com.elearning.management.elearning_service.domain.ELearningComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ELearningComponentRepository extends JpaRepository<ELearningComponent, UUID> {

    Optional<ELearningComponent> findById(UUID id);

    Page<ELearningComponent> findByType(ComponentType type, Pageable pageable);

    Page<ELearningComponent> findByCategory(ComponentCategory category, Pageable pageable);
}

