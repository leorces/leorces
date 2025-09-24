package com.leorces.model.runtime.activity;

import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Activity Tests")
class ActivityTest {

    private static final String TEST_ID = "activity-123";
    private static final String TEST_DEFINITION_ID = "definition-456";
    private static final ActivityState TEST_STATE = ActivityState.ACTIVE;
    private static final int TEST_RETRIES = 3;
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 11, 0);
    private static final LocalDateTime TEST_STARTED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime TEST_COMPLETED_AT = LocalDateTime.of(2024, 1, 15, 11, 30);

    @Test
    @DisplayName("Should create Activity with builder pattern")
    void shouldCreateActivityWithBuilder() {
        // Given
        var variable1 = Variable.builder()
                .id("var1")
                .varKey("key1")
                .varValue("value1")
                .build();
        var variable2 = Variable.builder()
                .id("var2")
                .varKey("key2")
                .varValue("value2")
                .build();
        var variables = List.of(variable1, variable2);

        // When
        var activity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .variables(variables)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .startedAt(TEST_STARTED_AT)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotNull(activity);
        assertEquals(TEST_ID, activity.id());
        assertEquals(TEST_DEFINITION_ID, activity.definitionId());
        assertEquals(variables, activity.variables());
        assertEquals(TEST_STATE, activity.state());
        assertEquals(TEST_RETRIES, activity.retries());
        assertEquals(TEST_CREATED_AT, activity.createdAt());
        assertEquals(TEST_UPDATED_AT, activity.updatedAt());
        assertEquals(TEST_STARTED_AT, activity.startedAt());
        assertEquals(TEST_COMPLETED_AT, activity.completedAt());
    }

    @Test
    @DisplayName("Should create Activity with minimal required fields")
    void shouldCreateActivityWithMinimalFields() {
        // Given & When
        var activity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(0)
                .build();

        // Then
        assertNotNull(activity);
        assertEquals(TEST_ID, activity.id());
        assertEquals(TEST_DEFINITION_ID, activity.definitionId());
        assertEquals(TEST_STATE, activity.state());
        assertEquals(0, activity.retries());
        assertNull(activity.variables());
        assertNull(activity.createdAt());
        assertNull(activity.updatedAt());
        assertNull(activity.startedAt());
        assertNull(activity.completedAt());
    }

    @Test
    @DisplayName("Should handle empty variables list")
    void shouldHandleEmptyVariablesList() {
        // Given
        var emptyVariables = List.<Variable>of();

        // When
        var activity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .variables(emptyVariables)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // Then
        assertNotNull(activity);
        assertEquals(emptyVariables, activity.variables());
        assertTrue(activity.variables().isEmpty());
    }

    @Test
    @DisplayName("Should handle different activity states")
    void shouldHandleDifferentActivityStates() {
        // Given & When
        var scheduledActivity = Activity.builder()
                .id("scheduled-activity")
                .definitionId(TEST_DEFINITION_ID)
                .state(ActivityState.SCHEDULED)
                .retries(0)
                .build();

        var completedActivity = Activity.builder()
                .id("completed-activity")
                .definitionId(TEST_DEFINITION_ID)
                .state(ActivityState.COMPLETED)
                .retries(TEST_RETRIES)
                .build();

        var failedActivity = Activity.builder()
                .id("failed-activity")
                .definitionId(TEST_DEFINITION_ID)
                .state(ActivityState.FAILED)
                .retries(5)
                .build();

        // Then
        assertEquals(ActivityState.SCHEDULED, scheduledActivity.state());
        assertEquals(ActivityState.COMPLETED, completedActivity.state());
        assertEquals(ActivityState.FAILED, failedActivity.state());
        assertEquals(0, scheduledActivity.retries());
        assertEquals(TEST_RETRIES, completedActivity.retries());
        assertEquals(5, failedActivity.retries());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalActivity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .createdAt(TEST_CREATED_AT)
                .build();

        // When
        var modifiedActivity = originalActivity.toBuilder()
                .state(ActivityState.COMPLETED)
                .retries(0)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotEquals(originalActivity, modifiedActivity);
        assertEquals(TEST_ID, modifiedActivity.id());
        assertEquals(TEST_DEFINITION_ID, modifiedActivity.definitionId());
        assertEquals(ActivityState.COMPLETED, modifiedActivity.state());
        assertEquals(0, modifiedActivity.retries());
        assertEquals(TEST_CREATED_AT, modifiedActivity.createdAt());
        assertEquals(TEST_COMPLETED_AT, modifiedActivity.completedAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var activity1 = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        var activity2 = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        var activity3 = Activity.builder()
                .id("different-id")
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When & Then
        assertEquals(activity1, activity2);
        assertNotEquals(activity1, activity3);
        assertNotEquals(null, activity1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var activity1 = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        var activity2 = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When & Then
        assertEquals(activity1.hashCode(), activity2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var activity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var toStringResult = activity.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Activity"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_DEFINITION_ID));
        assertTrue(toStringResult.contains(TEST_STATE.toString()));
        assertTrue(toStringResult.contains(String.valueOf(TEST_RETRIES)));
    }

    @Test
    @DisplayName("Should handle negative retries value")
    void shouldHandleNegativeRetriesValue() {
        // Given & When
        var activity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(ActivityState.FAILED)
                .retries(-1)
                .build();

        // Then
        assertNotNull(activity);
        assertEquals(-1, activity.retries());
    }

    @Test
    @DisplayName("Should handle LocalDateTime fields correctly")
    void shouldHandleLocalDateTimeFieldsCorrectly() {
        // Given
        var now = LocalDateTime.now();

        // When
        var activity = Activity.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .createdAt(now)
                .updatedAt(now)
                .startedAt(now)
                .completedAt(now)
                .build();

        // Then
        assertNotNull(activity);
        assertEquals(now, activity.createdAt());
        assertEquals(now, activity.updatedAt());
        assertEquals(now, activity.startedAt());
        assertEquals(now, activity.completedAt());
    }
}