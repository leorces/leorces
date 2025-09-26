package com.leorces.model.definition.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Activity Type Tests")
class ActivityTypeTest {

    private static final int EXPECTED_ENUM_COUNT = 23;

    @Test
    @DisplayName("Should contain all expected activity types")
    void shouldContainAllExpectedActivityTypes() {
        // Given
        var activityTypes = ActivityType.values();

        // When & Then
        assertEquals(EXPECTED_ENUM_COUNT, activityTypes.length);

        // Task types
        assertTrue(contains(activityTypes, ActivityType.EXTERNAL_TASK));
        assertTrue(contains(activityTypes, ActivityType.RECEIVE_TASK));

        // Gateway types
        assertTrue(contains(activityTypes, ActivityType.PARALLEL_GATEWAY));
        assertTrue(contains(activityTypes, ActivityType.INCLUSIVE_GATEWAY));
        assertTrue(contains(activityTypes, ActivityType.EXCLUSIVE_GATEWAY));
        assertTrue(contains(activityTypes, ActivityType.EVENT_BASED_GATEWAY));

        // Event types
        assertTrue(contains(activityTypes, ActivityType.START_EVENT));
        assertTrue(contains(activityTypes, ActivityType.MESSAGE_START_EVENT));
        assertTrue(contains(activityTypes, ActivityType.END_EVENT));
        assertTrue(contains(activityTypes, ActivityType.INTERMEDIATE_CATCH_EVENT));
        assertTrue(contains(activityTypes, ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT));
        assertTrue(contains(activityTypes, ActivityType.ERROR_END_EVENT));
        assertTrue(contains(activityTypes, ActivityType.ERROR_START_EVENT));
        assertTrue(contains(activityTypes, ActivityType.TERMINATE_END_EVENT));

        // Boundary Event types
        assertTrue(contains(activityTypes, ActivityType.TIMER_BOUNDARY_EVENT));
        assertTrue(contains(activityTypes, ActivityType.MESSAGE_BOUNDARY_EVENT));
        assertTrue(contains(activityTypes, ActivityType.ERROR_BOUNDARY_EVENT));
        assertTrue(contains(activityTypes, ActivityType.SIGNAL_BOUNDARY_EVENT));
        assertTrue(contains(activityTypes, ActivityType.CONDITIONAL_BOUNDARY_EVENT));
        assertTrue(contains(activityTypes, ActivityType.ESCALATION_BOUNDARY_EVENT));

        // Subprocess types
        assertTrue(contains(activityTypes, ActivityType.SUBPROCESS));
        assertTrue(contains(activityTypes, ActivityType.EVENT_SUBPROCESS));
        assertTrue(contains(activityTypes, ActivityType.CALL_ACTIVITY));
    }

    @ParameterizedTest
    @EnumSource(ActivityType.class)
    @DisplayName("Should have non-null name for all activity types")
    void shouldHaveNonNullNameForAllActivityTypes(ActivityType activityType) {
        // When
        var name = activityType.name();

        // Then
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(ActivityType.class)
    @DisplayName("Should have non-null toString for all activity types")
    void shouldHaveNonNullToStringForAllActivityTypes(ActivityType activityType) {
        // When
        var toString = activityType.toString();

        // Then
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertEquals(activityType.name(), toString);
    }

    @Test
    @DisplayName("Should support valueOf functionality")
    void shouldSupportValueOfFunctionality() {
        // Given
        var expectedType = ActivityType.EXTERNAL_TASK;

        // When
        var actualType = ActivityType.valueOf("EXTERNAL_TASK");

        // Then
        assertEquals(expectedType, actualType);
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Given
        var invalidName = "INVALID_ACTIVITY_TYPE";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ActivityType.valueOf(invalidName));
    }

    @Test
    @DisplayName("Should maintain enum equality and identity")
    void shouldMaintainEnumEqualityAndIdentity() {
        // Given
        var type1 = ActivityType.START_EVENT;
        var type2 = ActivityType.START_EVENT;
        var type3 = ActivityType.END_EVENT;

        // When & Then
        assertEquals(type1, type2);
        assertSame(type1, type2);
        assertNotEquals(type1, type3);
        assertNotSame(type1, type3);
    }

    @Test
    @DisplayName("Should support ordinal functionality")
    void shouldSupportOrdinalFunctionality() {
        // Given
        var activityTypes = ActivityType.values();

        // When & Then
        for (int i = 0; i < activityTypes.length; i++) {
            assertEquals(i, activityTypes[i].ordinal());
        }
    }

    @Test
    @DisplayName("Should maintain consistent hashCode")
    void shouldMaintainConsistentHashCode() {
        // Given
        var type1 = ActivityType.PARALLEL_GATEWAY;
        var type2 = ActivityType.PARALLEL_GATEWAY;

        // When
        var hashCode1 = type1.hashCode();
        var hashCode2 = type2.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    private boolean contains(ActivityType[] activityTypes, ActivityType target) {
        for (ActivityType type : activityTypes) {
            if (type == target) {
                return true;
            }
        }
        return false;
    }

}