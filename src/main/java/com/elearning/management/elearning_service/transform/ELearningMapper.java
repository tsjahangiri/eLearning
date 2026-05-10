package com.elearning.management.elearning_service.transform;

import com.elearning.management.elearning_service.domain.ELearningComponent;
import com.elearning.management.elearning_service.domain.MetaTag;
import com.elearning.management.elearning_service.domain.UserAssignment;
import com.elearning.management.elearning_service.dto.projection.AssignedComponentProjection;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.dto.response.AssignedDatesResponse;
import com.elearning.management.elearning_service.dto.response.AvailableDatesResponse;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ELearningMapper {

    // ─── List endpoint ─────────────────────────────────────────────────────

    @Mapping(target = "id", source = "componentId")
    @Mapping(target = "name", source = "componentName")
    @Mapping(target = "type", source = "componentType")
    @Mapping(target = "imageUrl", source = "componentImageUrl")
    @Mapping(target = "userStatus", source = "assignmentStatus")
    @Mapping(target = "assignedDates", source = "projection",
            qualifiedByName = "projectionToAssignedDates")
    AssignedComponentResponse toAssignedComponentResponse(
            AssignedComponentProjection projection);

    @Named("projectionToAssignedDates")
    default AssignedDatesResponse projectionToAssignedDates(
            final AssignedComponentProjection projection) {
        if (projection.assignedStartDate() == null
                && projection.assignedEndDate() == null) {
            return null;
        }
        return AssignedDatesResponse.builder()
                .startDate(projection.assignedStartDate())
                .endDate(projection.assignedEndDate())
                .build();
    }

    // ─── Detail endpoint ───────────────────────────────────────────────────

    @Mapping(target = "id", source = "component.id")
    @Mapping(target = "name", source = "component.name")
    @Mapping(target = "description", source = "component.description")
    @Mapping(target = "type", source = "component.type")
    @Mapping(target = "imageUrl", source = "component.imageUrl")
    @Mapping(target = "category", source = "component.category")
    @Mapping(target = "userStatus", source = "assignment.status")
    @Mapping(target = "availableDates", source = "component",
            qualifiedByName = "toAvailableDates")
    @Mapping(target = "metaTags", source = "component",
            qualifiedByName = "toMetaTagNames")
    @Mapping(target = "duration", source = "component.durationInMinutes",
            qualifiedByName = "formatDuration")
    ComponentDetailResponse toComponentDetailResponse(
            ELearningComponent component,
            UserAssignment assignment);

    @Named("toAvailableDates")
    default AvailableDatesResponse toAvailableDates(
            final ELearningComponent component) {
        if (component.getAvailableStartDate() == null
                && component.getAvailableEndDate() == null) {
            return null;
        }
        return AvailableDatesResponse.builder()
                .startDate(component.getAvailableStartDate())
                .endDate(component.getAvailableEndDate())
                .build();
    }

    @Named("toMetaTagNames")
    default Set<String> toMetaTagNames(final ELearningComponent component) {
        if (component.getMetaTags() == null
                || component.getMetaTags().isEmpty()) {
            return Set.of();
        }
        return component.getMetaTags()
                .stream()
                .map(MetaTag::getName)
                .collect(Collectors.toSet());
    }

    @Named("formatDuration")
    default String formatDuration(final Integer durationInMinutes) {
        if (durationInMinutes == null) return null;
        if (durationInMinutes < 60) return durationInMinutes + " minutes";
        if (durationInMinutes % 60 == 0)
            return (durationInMinutes / 60) + " hours";
        return (durationInMinutes / 60) + "h "
                + (durationInMinutes % 60) + "m";
    }
}

