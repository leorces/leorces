package com.leorces.model.runtime.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Activity State Tests")
class ActivityStateTest {

    private static final int EXPECTED_ENUM_COUNT = 6;

    @Test
    @DisplayName("Should contain all expected activity states")
    void shouldContainAllExpectedActivityStates() {
        // Given
        var activityStates = ActivityState.values();

        // When & Then
        assertEquals(EXPECTED_ENUM_COUNT, activityStates.length);
        assertTrue(contains(activityStates, ActivityState.SCHEDULED));
        assertTrue(contains(activityStates, ActivityState.ACTIVE));
        assertTrue(contains(activityStates, ActivityState.COMPLETED));
        assertTrue(contains(activityStates, ActivityState.CANCELED));
        assertTrue(contains(activityStates, ActivityState.TERMINATED));
        assertTrue(contains(activityStates, ActivityState.FAILED));
    }

    @ParameterizedTest
    @EnumSource(ActivityState.class)
    @DisplayName("Should have non-null name for all activity states")
    void shouldHaveNonNullNameForAllActivityStates(ActivityState activityState) {
        // When
        var name = activityState.name();

        // Then
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(ActivityState.class)
    @DisplayName("Should have non-null toString for all activity states")
    void shouldHaveNonNullToStringForAllActivityStates(ActivityState activityState) {
        // When
        var toString = activityState.toString();

        // Then
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertEquals(activityState.name(), toString);
    }

    @Test
    @DisplayName("Should support valueOf functionality")
    void shouldSupportValueOfFunctionality() {
        // Given
        var expectedState = ActivityState.SCHEDULED;

        // When
        var actualState = ActivityState.valueOf("SCHEDULED");

        // Then
        assertEquals(expectedState, actualState);
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Given
        var invalidName = "INVALID_ACTIVITY_STATE";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ActivityState.valueOf(invalidName));
    }

    @Test
    @DisplayName("Should maintain enum equality and identity")
    void shouldMaintainEnumEqualityAndIdentity() {
        // Given
        var state1 = ActivityState.ACTIVE;
        var state2 = ActivityState.ACTIVE;
        var state3 = ActivityState.COMPLETED;

        // When & Then
        assertEquals(state1, state2);
        assertSame(state1, state2);
        assertNotEquals(state1, state3);
        assertNotSame(state1, state3);
    }

    @Test
    @DisplayName("Should support ordinal functionality")
    void shouldSupportOrdinalFunctionality() {
        // Given
        var activityStates = ActivityState.values();

        // When & Then
        for (int i = 0; i < activityStates.length; i++) {
            assertEquals(i, activityStates[i].ordinal());
        }
    }

    @Test
    @DisplayName("Should maintain consistent hashCode")
    void shouldMaintainConsistentHashCode() {
        // Given
        var state1 = ActivityState.FAILED;
        var state2 = ActivityState.FAILED;

        // When
        var hashCode1 = state1.hashCode();
        var hashCode2 = state2.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("Should verify activity state progression semantics")
    void shouldVerifyActivityStateProgressionSemantics() {
        // Given
        var states = ActivityState.values();

        // When & Then
        // Verify order makes semantic sense
        assertEquals(ActivityState.SCHEDULED, states[0]);
        assertEquals(ActivityState.ACTIVE, states[1]);
        assertEquals(ActivityState.COMPLETED, states[2]);
        assertEquals(ActivityState.CANCELED, states[3]);
        assertEquals(ActivityState.TERMINATED, states[4]);
        assertEquals(ActivityState.FAILED, states[5]);
    }

    @Test
    @DisplayName("Should distinguish between different end states")
    void shouldDistinguishBetweenDifferentEndStates() {
        // Given
        var completedState = ActivityState.COMPLETED;
        var canceledState = ActivityState.CANCELED;
        var terminatedState = ActivityState.TERMINATED;
        var failedState = ActivityState.FAILED;

        // When & Then
        assertNotEquals(completedState, canceledState);
        assertNotEquals(completedState, terminatedState);
        assertNotEquals(completedState, failedState);
        assertNotEquals(canceledState, terminatedState);
        assertNotEquals(canceledState, failedState);
        assertNotEquals(terminatedState, failedState);
    }

    private boolean contains(ActivityState[] activityStates, ActivityState target) {
        for (ActivityState state : activityStates) {
            if (state == target) {
                return true;
            }
        }
        return false;
    }
}