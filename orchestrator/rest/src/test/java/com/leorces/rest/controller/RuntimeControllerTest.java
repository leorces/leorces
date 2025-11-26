package com.leorces.rest.controller;

import com.leorces.api.RuntimeService;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.model.request.CorrelateMessageRequest;
import com.leorces.rest.model.request.ProcessModificationRequest;
import com.leorces.rest.model.request.StartProcessByIdRequest;
import com.leorces.rest.model.request.StartProcessByKeyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuntimeController Tests")
class RuntimeControllerTest {

    private static final String TEST_DEFINITION_KEY = "test-definition-key";
    private static final String TEST_DEFINITION_ID = "test-definition-id";
    private static final String TEST_BUSINESS_KEY = "test-business-key";
    private static final String TEST_EXECUTION_ID = "test-execution-id";
    private static final String TEST_MESSAGE = "test-message";
    private static final String TEST_VARIABLE_KEY = "testKey";
    private static final String TEST_VARIABLE_VALUE = "testValue";

    @Mock
    private RuntimeService runtimeService;

    private RuntimeController subject;

    @BeforeEach
    void setUp() {
        // Given
        subject = new RuntimeController(runtimeService);
    }

    @Test
    @DisplayName("Should start process by key successfully")
    void shouldStartProcessByKeySuccessfully() {
        // Given
        var variables = Map.<String, Object>of(TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE);
        var request = new StartProcessByKeyRequest(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, variables);
        var expectedProcess = createTestProcess();

        when(runtimeService.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, variables))
                .thenReturn(expectedProcess);

        // When
        var result = subject.startProcessByKey(request).getBody();

        // Then
        assertThat(result).isEqualTo(expectedProcess);
        verify(runtimeService).startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, variables);
    }

    @Test
    @DisplayName("Should start process by key with null variables")
    void shouldStartProcessByKeyWithNullVariables() {
        // Given
        var request = new StartProcessByKeyRequest(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, null);
        var expectedProcess = createTestProcess();

        when(runtimeService.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, null))
                .thenReturn(expectedProcess);

        // When
        var result = subject.startProcessByKey(request).getBody();

        // Then
        assertThat(result).isEqualTo(expectedProcess);
        verify(runtimeService).startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, null);
    }

    @Test
    @DisplayName("Should start process by ID successfully")
    void shouldStartProcessByIdSuccessfully() {
        // Given
        var variables = Map.<String, Object>of(TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE);
        var request = new StartProcessByIdRequest(TEST_DEFINITION_ID, TEST_BUSINESS_KEY, variables);
        var expectedProcess = createTestProcess();

        when(runtimeService.startProcessById(TEST_DEFINITION_ID, TEST_BUSINESS_KEY, variables))
                .thenReturn(expectedProcess);

        // When
        var result = subject.startProcessById(request).getBody();

        // Then
        assertThat(result).isEqualTo(expectedProcess);
        verify(runtimeService).startProcessById(TEST_DEFINITION_ID, TEST_BUSINESS_KEY, variables);
    }

    @Test
    @DisplayName("Should terminate process successfully")
    void shouldTerminateProcessSuccessfully() {
        // When
        var response = subject.terminateProcess(TEST_EXECUTION_ID);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(runtimeService).terminateProcess(TEST_EXECUTION_ID);
    }

    @Test
    @DisplayName("Should modify process successfully")
    void shouldModifyProcessSuccessfully() {
        // Given
        var request = new ProcessModificationRequest("activity-123", "target-def-456");

        // When
        var response = subject.modifyProcess(TEST_EXECUTION_ID, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(runtimeService).moveExecution(TEST_EXECUTION_ID, "activity-123", "target-def-456");
    }

    @Test
    @DisplayName("Should modify process with different activity and target definition")
    void shouldModifyProcessWithDifferentIds() {
        // Given
        var request = new ProcessModificationRequest("another-activity", "another-target");

        // When
        var response = subject.modifyProcess(TEST_EXECUTION_ID, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(runtimeService).moveExecution(TEST_EXECUTION_ID, "another-activity", "another-target");
    }

    @Test
    @DisplayName("Should correlate message with business key, correlation keys, and process variables")
    void shouldCorrelateMessageWithAllParameters() {
        // Given
        var correlationKeys = Map.<String, Object>of("correlationKey", "value");
        var processVariables = Map.<String, Object>of("processVar", "value");
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, TEST_BUSINESS_KEY, correlationKeys, processVariables
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(
                TEST_MESSAGE, TEST_BUSINESS_KEY, correlationKeys, processVariables
        );
    }

    @Test
    @DisplayName("Should correlate message with correlation keys and process variables only")
    void shouldCorrelateMessageWithCorrelationKeysAndProcessVariables() {
        // Given
        var correlationKeys = Map.<String, Object>of("correlationKey", "value");
        var processVariables = Map.<String, Object>of("processVar", "value");
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, null, correlationKeys, processVariables
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(TEST_MESSAGE, correlationKeys, processVariables);
    }

    @Test
    @DisplayName("Should correlate message with business key and process variables only")
    void shouldCorrelateMessageWithBusinessKeyAndProcessVariables() {
        // Given
        var processVariables = Map.<String, Object>of("processVar", "value");
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, TEST_BUSINESS_KEY, null, processVariables
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(TEST_MESSAGE, TEST_BUSINESS_KEY, processVariables);
    }

    @Test
    @DisplayName("Should correlate message with correlation keys only")
    void shouldCorrelateMessageWithCorrelationKeysOnly() {
        // Given
        var correlationKeys = Map.<String, Object>of("correlationKey", "value");
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, null, correlationKeys, null
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(TEST_MESSAGE, correlationKeys);
    }

    @Test
    @DisplayName("Should correlate message with business key only")
    void shouldCorrelateMessageWithBusinessKeyOnly() {
        // Given
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, TEST_BUSINESS_KEY, null, null
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(TEST_MESSAGE, TEST_BUSINESS_KEY);
    }

    @Test
    @DisplayName("Should correlate message with empty correlation keys and process variables")
    void shouldCorrelateMessageWithEmptyCollections() {
        // Given
        var emptyCorrelationKeys = Map.<String, Object>of();
        var emptyProcessVariables = Map.<String, Object>of();
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, TEST_BUSINESS_KEY, emptyCorrelationKeys, emptyProcessVariables
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(TEST_MESSAGE, TEST_BUSINESS_KEY);
    }

    @Test
    @DisplayName("Should correlate message with whitespace-only business key")
    void shouldCorrelateMessageWithWhitespaceBusinessKey() {
        // Given
        var correlationKeys = Map.<String, Object>of("correlationKey", "value");
        var request = new CorrelateMessageRequest(
                TEST_MESSAGE, "   ", correlationKeys, null
        );

        // When
        subject.correlateMessage(request);

        // Then
        verify(runtimeService).correlateMessage(TEST_MESSAGE, correlationKeys);
    }

    @Test
    @DisplayName("Should set variables successfully")
    void shouldSetVariablesSuccessfully() {
        // Given
        var variables = Map.<String, Object>of(TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE);

        // When
        subject.setVariables(TEST_EXECUTION_ID, variables);

        // Then
        verify(runtimeService).setVariables(TEST_EXECUTION_ID, variables);
    }

    @Test
    @DisplayName("Should set local variables successfully")
    void shouldSetLocalVariablesSuccessfully() {
        // Given
        var variables = Map.<String, Object>of(TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE);

        // When
        subject.setVariablesLocal(TEST_EXECUTION_ID, variables);

        // Then
        verify(runtimeService).setVariablesLocal(TEST_EXECUTION_ID, variables);
    }

    @Test
    @DisplayName("Should set variables with complex data types")
    void shouldSetVariablesWithComplexDataTypes() {
        // Given
        var complexVariables = Map.of(
                "stringVar", "test",
                "numberVar", 42,
                "booleanVar", true,
                "listVar", java.util.List.of("item1", "item2"),
                "mapVar", Map.of("nestedKey", "nestedValue")
        );

        // When
        subject.setVariables(TEST_EXECUTION_ID, complexVariables);

        // Then
        verify(runtimeService).setVariables(TEST_EXECUTION_ID, complexVariables);
    }

    @Test
    @DisplayName("Should set local variables with empty map")
    void shouldSetLocalVariablesWithEmptyMap() {
        // Given
        var emptyVariables = Map.<String, Object>of();

        // When
        subject.setVariablesLocal(TEST_EXECUTION_ID, emptyVariables);

        // Then
        verify(runtimeService).setVariablesLocal(TEST_EXECUTION_ID, emptyVariables);
    }

    @Test
    @DisplayName("Should find process")
    void shouldFindProcess() {
        var filter = ProcessFilter.builder().businessKey(TEST_BUSINESS_KEY).build();
        var expected = createTestProcess();

        when(runtimeService.findProcess(filter)).thenReturn(expected);

        var res = subject.findSingleProcess(filter);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(expected);
        verify(runtimeService).findProcess(filter);
    }

    @Test
    @DisplayName("Should return 404 if process not found")
    void shouldReturn404IfNotFound() {
        var filter = ProcessFilter.builder().businessKey("none").build();

        when(runtimeService.findProcess(filter)).thenReturn(null);

        var res = subject.findSingleProcess(filter);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Process createTestProcess() {
        return Process.builder()
                .id("test-process-id")
                .businessKey(TEST_BUSINESS_KEY)
                .build();
    }

}