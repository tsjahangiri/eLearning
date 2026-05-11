package com.elearning.management.elearning_service.repository;

import com.elearning.management.elearning_service.domain.MetaTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetaTagRepository extends JpaRepository<MetaTag, UUID> {

}

