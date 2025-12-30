package com.leorces.rest.client.service;

import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.client.client.RuntimeClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Runtime Service Implementation Tests")
class RuntimeServiceImplTest {

    private static final String DEFINITION_ID = "test-definition-123";
    private static final String PROCESS_KEY = "test-process-key";
    private static final String BUSINESS_KEY = "test-business-key";
    private static final String EXECUTION_ID = "test-execution-456";
    private static final String MESSAGE_NAME = "test-message";
    private static final String VARIABLE_KEY = "testKey";
    private static final String VARIABLE_VALUE = "testValue";
    private static final Map<String, Object> VARIABLES = Map.of("key1", "value1", "key2", 42);
    private static final Map<String, Object> CORRELATION_KEYS = Map.of("correlationKey", "correlationValue");
    private static final Map<String, Object> EMPTY_MAP = Map.of();

    @Mock
    private RuntimeClient runtimeClient;

    @InjectMocks
    private RuntimeServiceImpl runtimeService;

    @Test
    @DisplayName("Should start process by ID with default parameters")
    void shouldStartProcessByIdWithDefaultParameters() {
        //Given
        var expectedProcess = createProcess("process-1");
        when(runtimeClient.startProcessById(DEFINITION_ID, null, EMPTY_MAP))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID);

        //Then
        verify(runtimeClient).startProcessById(DEFINITION_ID, null, EMPTY_MAP);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by ID with variables")
    void shouldStartProcessByIdWithVariables() {
        //Given
        var expectedProcess = createProcess("process-2");
        when(runtimeClient.startProcessById(DEFINITION_ID, null, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID, VARIABLES);

        //Then
        verify(runtimeClient).startProcessById(DEFINITION_ID, null, VARIABLES);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by ID with business key")
    void shouldStartProcessByIdWithBusinessKey() {
        //Given
        var expectedProcess = createProcess("process-3");
        when(runtimeClient.startProcessById(DEFINITION_ID, BUSINESS_KEY, EMPTY_MAP))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID, BUSINESS_KEY);

        //Then
        verify(runtimeClient).startProcessById(DEFINITION_ID, BUSINESS_KEY, EMPTY_MAP);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by ID with business key and variables")
    void shouldStartProcessByIdWithBusinessKeyAndVariables() {
        //Given
        var expectedProcess = createProcess("process-4");
        when(runtimeClient.startProcessById(DEFINITION_ID, BUSINESS_KEY, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID, BUSINESS_KEY, VARIABLES);

        //Then
        verify(runtimeClient).startProcessById(DEFINITION_ID, BUSINESS_KEY, VARIABLES);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by key with default parameters")
    void shouldStartProcessByKeyWithDefaultParameters() {
        //Given
        var expectedProcess = createProcess("process-5");
        when(runtimeClient.startProcessByKey(PROCESS_KEY, null, EMPTY_MAP))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(PROCESS_KEY);

        //Then
        verify(runtimeClient).startProcessByKey(PROCESS_KEY, null, EMPTY_MAP);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by key with variables")
    void shouldStartProcessByKeyWithVariables() {
        //Given
        var expectedProcess = createProcess("process-6");
        when(runtimeClient.startProcessByKey(PROCESS_KEY, null, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(PROCESS_KEY, VARIABLES);

        //Then
        verify(runtimeClient).startProcessByKey(PROCESS_KEY, null, VARIABLES);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by key with business key")
    void shouldStartProcessByKeyWithBusinessKey() {
        //Given
        var expectedProcess = createProcess("process-7");
        when(runtimeClient.startProcessByKey(PROCESS_KEY, BUSINESS_KEY, EMPTY_MAP))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(PROCESS_KEY, BUSINESS_KEY);

        //Then
        verify(runtimeClient).startProcessByKey(PROCESS_KEY, BUSINESS_KEY, EMPTY_MAP);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should start process by key with business key and variables")
    void shouldStartProcessByKeyWithBusinessKeyAndVariables() {
        //Given
        var expectedProcess = createProcess("process-8");
        when(runtimeClient.startProcessByKey(PROCESS_KEY, BUSINESS_KEY, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(PROCESS_KEY, BUSINESS_KEY, VARIABLES);

        //Then
        verify(runtimeClient).startProcessByKey(PROCESS_KEY, BUSINESS_KEY, VARIABLES);
        assertThat(result).isEqualTo(expectedProcess);
    }

    @Test
    @DisplayName("Should terminate process by ID")
    void shouldTerminateProcess() {
        //When
        runtimeService.terminateProcess(EXECUTION_ID);

        //Then
        verify(runtimeClient).terminateProcess(EXECUTION_ID);
    }

    @Test
    @DisplayName("Should retry all failed activities in process by ID")
    void shouldResolveIncident() {
        //When
        runtimeService.resolveIncident(EXECUTION_ID);

        //Then
        verify(runtimeClient).resolveIncident(EXECUTION_ID);
    }

    @Test
    @DisplayName("Should suspend process by process ID")
    void shouldSuspendProcessById() {
        // When
        runtimeService.suspendProcessById(EXECUTION_ID);

        // Then
        verify(runtimeClient).suspendProcessById(EXECUTION_ID);
    }

    @Test
    @DisplayName("Should suspend processes by definition ID")
    void shouldSuspendProcessesByDefinitionId() {
        // When
        runtimeService.suspendProcessesByDefinitionId(DEFINITION_ID);

        // Then
        verify(runtimeClient).suspendProcessesByDefinitionId(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should suspend processes by definition key")
    void shouldSuspendProcessesByDefinitionKey() {
        // When
        runtimeService.suspendProcessesByDefinitionKey(PROCESS_KEY);

        // Then
        verify(runtimeClient).suspendProcessesByDefinitionKey(PROCESS_KEY);
    }

    @Test
    @DisplayName("Should resume process by process ID")
    void shouldResumeProcessById() {
        // When
        runtimeService.resumeProcessById(EXECUTION_ID);

        // Then
        verify(runtimeClient).resumeProcessById(EXECUTION_ID);
    }

    @Test
    @DisplayName("Should resume processes by definition ID")
    void shouldResumeProcessesByDefinitionId() {
        // When
        runtimeService.resumeProcessesByDefinitionId(DEFINITION_ID);

        // Then
        verify(runtimeClient).resumeProcessesByDefinitionId(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should resume processes by definition key")
    void shouldResumeProcessesByDefinitionKey() {
        // When
        runtimeService.resumeProcessesByDefinitionKey(PROCESS_KEY);

        // Then
        verify(runtimeClient).resumeProcessesByDefinitionKey(PROCESS_KEY);
    }

    @Test
    @DisplayName("Should move execution by delegating to RuntimeClient")
    void shouldMoveExecution() {
        // When
        runtimeService.moveExecution(EXECUTION_ID, "activity-123", "target-def-456");

        // Then
        verify(runtimeClient).moveExecution(EXECUTION_ID, "activity-123", "target-def-456");
    }


    @Test
    @DisplayName("Should set single variable")
    void shouldSetSingleVariable() {
        //When
        runtimeService.setVariable(EXECUTION_ID, VARIABLE_KEY, VARIABLE_VALUE);

        //Then
        verify(runtimeClient).setVariables(EXECUTION_ID, Map.of(VARIABLE_KEY, VARIABLE_VALUE));
    }

    @Test
    @DisplayName("Should set multiple variables")
    void shouldSetMultipleVariables() {
        //When
        runtimeService.setVariables(EXECUTION_ID, VARIABLES);

        //Then
        verify(runtimeClient).setVariables(EXECUTION_ID, VARIABLES);
    }

    @Test
    @DisplayName("Should set single local variable")
    void shouldSetSingleLocalVariable() {
        //When
        runtimeService.setVariableLocal(EXECUTION_ID, VARIABLE_KEY, VARIABLE_VALUE);

        //Then
        verify(runtimeClient).setVariablesLocal(EXECUTION_ID, Map.of(VARIABLE_KEY, VARIABLE_VALUE));
    }

    @Test
    @DisplayName("Should set multiple local variables")
    void shouldSetMultipleLocalVariables() {
        //When
        runtimeService.setVariablesLocal(EXECUTION_ID, VARIABLES);

        //Then
        verify(runtimeClient).setVariablesLocal(EXECUTION_ID, VARIABLES);
    }

    @Test
    @DisplayName("Should correlate message with business key")
    void shouldCorrelateMessageWithBusinessKey() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, BUSINESS_KEY);

        //Then
        verify(runtimeClient).correlateMessage(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP);
    }

    @Test
    @DisplayName("Should correlate message with correlation keys")
    void shouldCorrelateMessageWithCorrelationKeys() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, CORRELATION_KEYS);

        //Then
        verify(runtimeClient).correlateMessage(MESSAGE_NAME, null, CORRELATION_KEYS, EMPTY_MAP);
    }

    @Test
    @DisplayName("Should correlate message with business key and process variables")
    void shouldCorrelateMessageWithBusinessKeyAndProcessVariables() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, BUSINESS_KEY, VARIABLES);

        //Then
        verify(runtimeClient).correlateMessage(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, VARIABLES);
    }

    @Test
    @DisplayName("Should correlate message with correlation keys and process variables")
    void shouldCorrelateMessageWithCorrelationKeysAndProcessVariables() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, CORRELATION_KEYS, VARIABLES);

        //Then
        verify(runtimeClient).correlateMessage(MESSAGE_NAME, null, CORRELATION_KEYS, VARIABLES);
    }

    @Test
    @DisplayName("Should correlate message with all parameters")
    void shouldCorrelateMessageWithAllParameters() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, BUSINESS_KEY, VARIABLES, CORRELATION_KEYS);

        //Then
        verify(runtimeClient).correlateMessage(MESSAGE_NAME, BUSINESS_KEY, VARIABLES, CORRELATION_KEYS);
    }

    @Test
    @DisplayName("Should find process using filter")
    void shouldFindProcess() {
        // Given
        var filter = ProcessFilter.builder()
                .processDefinitionKey("defKey")
                .businessKey("bizKey")
                .variables(Map.of("v1", "x"))
                .build();

        var expectedProcess = createProcess("process-found");

        when(runtimeClient.findProcess(filter))
                .thenReturn(expectedProcess);

        // When
        var result = runtimeService.findProcess(filter);

        // Then
        verify(runtimeClient).findProcess(filter);
        assertThat(result).isEqualTo(expectedProcess);
    }

    private Process createProcess(String id) {
        return Process.builder()
                .id(id)
                .build();
    }

}