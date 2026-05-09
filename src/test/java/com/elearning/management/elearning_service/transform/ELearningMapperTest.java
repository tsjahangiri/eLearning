package com.elearning.management.elearning_service.transform;

import com.elearning.management.elearning_service.TestFactory;
import com.elearning.management.elearning_service.domain.*;
import com.elearning.management.elearning_service.dto.response.AssignedComponentResponse;
import com.elearning.management.elearning_service.dto.response.ComponentDetailResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Set;

import static com.elearning.management.elearning_service.TestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

class ELearningMapperTest {

    private final ELearningMapper mapper = Mappers.getMapper(ELearningMapper.class);

    // ─── toAssignedComponentResponse ───────────────────────────────────────

    @Test
    void toAssignedComponentResponse_mapsAllFieldsCorrectly() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);

        final AssignedComponentResponse result =
                mapper.toAssignedComponentResponse(assignment);

        assertThat(result.getId()).isEqualTo(component.getId());
        assertThat(result.getName()).isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(result.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(result.getUserStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/scrum.jpg");
    }

    @Test
    void toAssignedComponentResponse_withAssignedDates_mapsCorrectly() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);
        assignment.setAssignedStartDate(LocalDate.of(2024, 1, 15));
        assignment.setAssignedEndDate(LocalDate.of(2024, 6, 15));

        final AssignedComponentResponse result =
                mapper.toAssignedComponentResponse(assignment);

        assertThat(result.getAssignedDates()).isNotNull();
        assertThat(result.getAssignedDates().getStartDate())
                .isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(result.getAssignedDates().getEndDate())
                .isEqualTo(LocalDate.of(2024, 6, 15));
    }

    @Test
    void toAssignedComponentResponse_withNullDates_returnNullAssignedDates() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);
        assignment.setAssignedStartDate(null);
        assignment.setAssignedEndDate(null);

        final AssignedComponentResponse result =
                mapper.toAssignedComponentResponse(assignment);

        assertThat(result.getAssignedDates()).isNull();
    }

    // ─── toComponentDetailResponse ─────────────────────────────────────────

    @Test
    void toComponentDetailResponse_mapsAllFieldsCorrectly() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getId()).isEqualTo(component.getId());
        assertThat(result.getName()).isEqualTo(DEFAULT_COMPONENT_NAME);
        assertThat(result.getDescription())
                .isEqualTo("A comprehensive introduction to Scrum framework");
        assertThat(result.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(result.getImageUrl())
                .isEqualTo("http://example.com/scrum.jpg");
        assertThat(result.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(result.getUserStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void toComponentDetailResponse_durationFormattedAsHours() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setDurationInMinutes(480);
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getDuration()).isEqualTo("8 hours");
    }

    @Test
    void toComponentDetailResponse_durationFormattedAsMinutes() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setDurationInMinutes(45);
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getDuration()).isEqualTo("45 minutes");
    }

    @Test
    void toComponentDetailResponse_durationFormattedAsHoursAndMinutes() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setDurationInMinutes(90);
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getDuration()).isEqualTo("1h 30m");
    }

    @Test
    void toComponentDetailResponse_nullDuration_returnsNull() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setDurationInMinutes(null);
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getDuration()).isNull();
    }

    @Test
    void toComponentDetailResponse_withMetaTags_mapsToStringSet() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        final MetaTag tag1 = new MetaTag();
        tag1.setName("Scrum");
        final MetaTag tag2 = new MetaTag();
        tag2.setName("Agile");
        component.setMetaTags(Set.of(tag1, tag2));
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getMetaTags()).containsExactlyInAnyOrder("Scrum", "Agile");
    }

    @Test
    void toComponentDetailResponse_withNoMetaTags_returnsEmptySet() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setMetaTags(Set.of());
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getMetaTags()).isEmpty();
    }

    @Test
    void toComponentDetailResponse_withAvailableDates_mapsCorrectly() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setAvailableStartDate(LocalDate.of(2024, 1, 1));
        component.setAvailableEndDate(LocalDate.of(2024, 12, 31));
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getAvailableDates()).isNotNull();
        assertThat(result.getAvailableDates().getStartDate())
                .isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.getAvailableDates().getEndDate())
                .isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void toComponentDetailResponse_withNullAvailableDates_returnsNullDates() {
        final User user = buildUser("encoded");
        final ELearningComponent component = buildComponent();
        component.setAvailableStartDate(null);
        component.setAvailableEndDate(null);
        final UserAssignment assignment = buildAssignment(user, component);

        final ComponentDetailResponse result =
                mapper.toComponentDetailResponse(component, assignment);

        assertThat(result.getAvailableDates()).isNull();
    }
}

