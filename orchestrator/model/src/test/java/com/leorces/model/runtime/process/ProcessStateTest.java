package com.leorces.model.runtime.process;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Process State Tests")
class ProcessStateTest {

    private static final int EXPECTED_ENUM_COUNT = 5;

    @Test
    @DisplayName("Should contain all expected process states")
    void shouldContainAllExpectedProcessStates() {
        // Given
        var processStates = ProcessState.values();

        // When & Then
        assertEquals(EXPECTED_ENUM_COUNT, processStates.length);
        assertTrue(contains(processStates, ProcessState.ACTIVE));
        assertTrue(contains(processStates, ProcessState.COMPLETED));
        assertTrue(contains(processStates, ProcessState.TERMINATED));
        assertTrue(contains(processStates, ProcessState.INCIDENT));
        assertTrue(contains(processStates, ProcessState.DELETED));
    }

    @ParameterizedTest
    @EnumSource(ProcessState.class)
    @DisplayName("Should have non-null name for all process states")
    void shouldHaveNonNullNameForAllProcessStates(ProcessState processState) {
        // When
        var name = processState.name();

        // Then
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(ProcessState.class)
    @DisplayName("Should have non-null toString for all process states")
    void shouldHaveNonNullToStringForAllProcessStates(ProcessState processState) {
        // When
        var toString = processState.toString();

        // Then
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertEquals(processState.name(), toString);
    }

    @Test
    @DisplayName("Should support valueOf functionality")
    void shouldSupportValueOfFunctionality() {
        // Given
        var expectedState = ProcessState.ACTIVE;

        // When
        var actualState = ProcessState.valueOf("ACTIVE");

        // Then
        assertEquals(expectedState, actualState);
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Given
        var invalidName = "INVALID_PROCESS_STATE";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ProcessState.valueOf(invalidName));
    }

    @Test
    @DisplayName("Should maintain enum equality and identity")
    void shouldMaintainEnumEqualityAndIdentity() {
        // Given
        var state1 = ProcessState.ACTIVE;
        var state2 = ProcessState.ACTIVE;
        var state3 = ProcessState.COMPLETED;

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
        var processStates = ProcessState.values();

        // When & Then
        for (int i = 0; i < processStates.length; i++) {
            assertEquals(i, processStates[i].ordinal());
        }
    }

    @Test
    @DisplayName("Should maintain consistent hashCode")
    void shouldMaintainConsistentHashCode() {
        // Given
        var state1 = ProcessState.COMPLETED;
        var state2 = ProcessState.COMPLETED;

        // When
        var hashCode1 = state1.hashCode();
        var hashCode2 = state2.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    private boolean contains(ProcessState[] processStates, ProcessState target) {
        for (ProcessState state : processStates) {
            if (state == target) {
                return true;
            }
        }
        return false;
    }

}