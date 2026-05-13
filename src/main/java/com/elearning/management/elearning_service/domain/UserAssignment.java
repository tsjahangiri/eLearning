package com.elearning.management.elearning_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_component",
                columnNames = {"user_id", "component_id"}
        ),
        indexes = {
                @Index(
                        name = "idx_assignment_user_id",
                        columnList = "user_id"),
                @Index(
                        name = "idx_assignment_component_id",
                        columnList = "component_id"),
                @Index(
                        name = "idx_assignment_status",
                        columnList = "status"),
                @Index(
                        name = "idx_assignment_user_status",
                        columnList = "user_id, status")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class UserAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ELearningComponent component;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column
    private LocalDate assignedStartDate;

    @Column
    private LocalDate assignedEndDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

    @Version
    @Column(nullable = false)
    private Long version;
}
