package com.leorces.model.runtime.process;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Process Tests")
class ProcessTest {

    private static final String TEST_ID = "process-123";
    private static final String TEST_ROOT_PROCESS_ID = "root-process-456";
    private static final String TEST_PARENT_ID = "parent-789";
    private static final String TEST_BUSINESS_KEY = "business-key-001";
    private static final ProcessState TEST_STATE = ProcessState.ACTIVE;
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 11, 0);
    private static final LocalDateTime TEST_STARTED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime TEST_COMPLETED_AT = LocalDateTime.of(2024, 1, 15, 11, 30);
    private static final String TEST_DEFINITION_ID = "definition-123";
    private static final String TEST_DEFINITION_KEY = "test-process";

    @Test
    @DisplayName("Should create Process with builder pattern")
    void shouldCreateProcessWithBuilder() {
        // Given
        var definition = createTestProcessDefinition();
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
        var process = Process.builder()
                .id(TEST_ID)
                .rootProcessId(TEST_ROOT_PROCESS_ID)
                .parentId(TEST_PARENT_ID)
                .businessKey(TEST_BUSINESS_KEY)
                .variables(variables)
                .state(TEST_STATE)
                .definition(definition)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .startedAt(TEST_STARTED_AT)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotNull(process);
        assertEquals(TEST_ID, process.id());
        assertEquals(TEST_ROOT_PROCESS_ID, process.rootProcessId());
        assertEquals(TEST_PARENT_ID, process.parentId());
        assertEquals(TEST_BUSINESS_KEY, process.businessKey());
        assertEquals(variables, process.variables());
        assertEquals(TEST_STATE, process.state());
        assertEquals(definition, process.definition());
        assertEquals(TEST_CREATED_AT, process.createdAt());
        assertEquals(TEST_UPDATED_AT, process.updatedAt());
        assertEquals(TEST_STARTED_AT, process.startedAt());
        assertEquals(TEST_COMPLETED_AT, process.completedAt());
    }

    @Test
    @DisplayName("Should create Process with minimal required fields")
    void shouldCreateProcessWithMinimalFields() {
        // Given
        var definition = createTestProcessDefinition();

        // When
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // Then
        assertNotNull(process);
        assertEquals(TEST_ID, process.id());
        assertEquals(TEST_STATE, process.state());
        assertEquals(definition, process.definition());
        assertNull(process.rootProcessId());
        assertNull(process.parentId());
        assertNull(process.businessKey());
        assertNull(process.createdAt());
        assertNull(process.updatedAt());
        assertNull(process.startedAt());
        assertNull(process.completedAt());
    }

    @Test
    @DisplayName("Should return empty list when variables is null")
    void shouldReturnEmptyListWhenVariablesIsNull() {
        // Given
        var definition = createTestProcessDefinition();
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .variables(null)
                .build();

        // When
        var variables = process.variables();

        // Then
        assertNotNull(variables);
        assertTrue(variables.isEmpty());
    }

    @Test
    @DisplayName("Should return actual variables list when not null")
    void shouldReturnActualVariablesListWhenNotNull() {
        // Given
        var definition = createTestProcessDefinition();
        var variable = Variable.builder()
                .id("var1")
                .varKey("key1")
                .varValue("value1")
                .build();
        var variablesList = List.of(variable);
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .variables(variablesList)
                .build();

        // When
        var variables = process.variables();

        // Then
        assertNotNull(variables);
        assertEquals(variablesList, variables);
        assertEquals(1, variables.size());
        assertEquals(variable, variables.getFirst());
    }

    @Test
    @DisplayName("Should return definition ID")
    void shouldReturnDefinitionId() {
        // Given
        var definition = createTestProcessDefinition();
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When
        var definitionId = process.definitionId();

        // Then
        assertEquals(TEST_DEFINITION_ID, definitionId);
    }

    @Test
    @DisplayName("Should return definition key")
    void shouldReturnDefinitionKey() {
        // Given
        var definition = createTestProcessDefinition();
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When
        var definitionKey = process.definitionKey();

        // Then
        assertEquals(TEST_DEFINITION_KEY, definitionKey);
    }

    @Test
    @DisplayName("Should return true for isCallActivity when parentId is not null")
    void shouldReturnTrueForIsCallActivityWhenParentIdIsNotNull() {
        // Given
        var definition = createTestProcessDefinition();
        var process = Process.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When
        var isCallActivity = process.isCallActivity();

        // Then
        assertTrue(isCallActivity);
    }

    @Test
    @DisplayName("Should return false for isCallActivity when parentId is null")
    void shouldReturnFalseForIsCallActivityWhenParentIdIsNull() {
        // Given
        var definition = createTestProcessDefinition();
        var process = Process.builder()
                .id(TEST_ID)
                .parentId(null)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When
        var isCallActivity = process.isCallActivity();

        // Then
        assertFalse(isCallActivity);
    }

    @Test
    @DisplayName("Should handle different process states")
    void shouldHandleDifferentProcessStates() {
        // Given
        var definition = createTestProcessDefinition();

        // When
        var activeProcess = Process.builder()
                .id("active-process")
                .state(ProcessState.ACTIVE)
                .definition(definition)
                .build();

        var completedProcess = Process.builder()
                .id("completed-process")
                .state(ProcessState.COMPLETED)
                .definition(definition)
                .build();

        var terminatedProcess = Process.builder()
                .id("terminated-process")
                .state(ProcessState.TERMINATED)
                .definition(definition)
                .build();

        // Then
        assertEquals(ProcessState.ACTIVE, activeProcess.state());
        assertEquals(ProcessState.COMPLETED, completedProcess.state());
        assertEquals(ProcessState.TERMINATED, terminatedProcess.state());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var definition = createTestProcessDefinition();
        var originalProcess = Process.builder()
                .id(TEST_ID)
                .state(ProcessState.ACTIVE)
                .definition(definition)
                .businessKey(TEST_BUSINESS_KEY)
                .build();

        // When
        var modifiedProcess = originalProcess.toBuilder()
                .state(ProcessState.COMPLETED)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotEquals(originalProcess, modifiedProcess);
        assertEquals(TEST_ID, modifiedProcess.id());
        assertEquals(ProcessState.COMPLETED, modifiedProcess.state());
        assertEquals(definition, modifiedProcess.definition());
        assertEquals(TEST_BUSINESS_KEY, modifiedProcess.businessKey());
        assertEquals(TEST_COMPLETED_AT, modifiedProcess.completedAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var process1 = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        var process2 = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        var process3 = Process.builder()
                .id("different-id")
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When & Then
        assertEquals(process1, process2);
        assertNotEquals(process1, process3);
        assertNotEquals(null, process1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var process1 = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        var process2 = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When & Then
        assertEquals(process1.hashCode(), process2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var process = Process.builder()
                .id(TEST_ID)
                .businessKey(TEST_BUSINESS_KEY)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When
        var toStringResult = process.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Process"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_BUSINESS_KEY));
        assertTrue(toStringResult.contains(TEST_STATE.toString()));
    }

    @Test
    @DisplayName("Should handle empty variables list")
    void shouldHandleEmptyVariablesList() {
        // Given
        var definition = createTestProcessDefinition();
        var emptyVariables = List.<Variable>of();
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .variables(emptyVariables)
                .build();

        // When
        var variables = process.variables();

        // Then
        assertNotNull(variables);
        assertEquals(emptyVariables, variables);
        assertTrue(variables.isEmpty());
    }

    @Test
    @DisplayName("Should handle LocalDateTime fields correctly")
    void shouldHandleLocalDateTimeFieldsCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var now = LocalDateTime.now();

        // When
        var process = Process.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .createdAt(now)
                .updatedAt(now)
                .startedAt(now)
                .completedAt(now)
                .build();

        // Then
        assertNotNull(process);
        assertEquals(now, process.createdAt());
        assertEquals(now, process.updatedAt());
        assertEquals(now, process.startedAt());
        assertEquals(now, process.completedAt());
    }

    private ProcessDefinition createTestProcessDefinition() {
        return ProcessDefinition.builder()
                .id(TEST_DEFINITION_ID)
                .key(TEST_DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .build();
    }
}