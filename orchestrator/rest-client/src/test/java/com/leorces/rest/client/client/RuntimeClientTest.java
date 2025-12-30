package com.leorces.rest.client.client;

import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.client.model.request.CorrelateMessageRequest;
import com.leorces.rest.client.model.request.ProcessModificationRequest;
import com.leorces.rest.client.model.request.StartProcessByIdRequest;
import com.leorces.rest.client.model.request.StartProcessByKeyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuntimeClient Tests")
class RuntimeClientTest {

    private static final String TEST_DEFINITION_KEY = "test-definition-key";
    private static final String TEST_DEFINITION_ID = "test-definition-id";
    private static final String TEST_BUSINESS_KEY = "test-business-key";
    private static final String TEST_EXECUTION_ID = "test-execution-id";
    private static final String TEST_MESSAGE = "test-message";
    private static final Map<String, Object> TEST_VARIABLES = Map.of("key", "value");
    private static final Map<String, Object> TEST_CORRELATION_KEYS = Map.of("correlationKey", "correlationValue");

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private RuntimeClient runtimeClient;

    private static Process createTestProcess(String id) {
        // Using mock since Process might be complex
        var process = mock(Process.class);
        when(process.id()).thenReturn(id);
        return process;
    }

    @BeforeEach
    void setUp() {
        runtimeClient = new RuntimeClient(restClient);
    }

    @Test
    @DisplayName("Should start process by key successfully when valid parameters are provided")
    void shouldStartProcessByKeySuccessfullyWhenValidParametersAreProvided() {
        // Given
        var expectedProcess = createTestProcess("process-1");
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(StartProcessByKeyRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class)).thenReturn(expectedProcess);

        // When
        var result = runtimeClient.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals("process-1", result.id());
        verify(restClient).post();
        verify(requestBodySpec).body(any(StartProcessByKeyRequest.class));
    }

    @Test
    @DisplayName("Should start process by id successfully when valid parameters are provided")
    void shouldStartProcessByIdSuccessfullyWhenValidParametersAreProvided() {
        // Given
        var expectedProcess = createTestProcess("process-2");
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(StartProcessByIdRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class)).thenReturn(expectedProcess);

        // When
        var result = runtimeClient.startProcessById(TEST_DEFINITION_ID, TEST_BUSINESS_KEY, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals("process-2", result.id());
        verify(restClient).post();
        verify(requestBodySpec).body(any(StartProcessByIdRequest.class));
    }

    @Test
    @DisplayName("Should terminate process successfully when valid processId is provided")
    void shouldTerminateProcessSuccessfully() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> runtimeClient.terminateProcess(TEST_EXECUTION_ID));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should handle bad request when terminating process")
    void shouldHandleBadRequestWhenTerminatingProcess() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> runtimeClient.terminateProcess(TEST_EXECUTION_ID));
    }

    @Test
    @DisplayName("Should throw server error when terminating process and server error occurs")
    void shouldThrowServerErrorWhenTerminatingProcess() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> runtimeClient.terminateProcess(TEST_EXECUTION_ID));
    }

    @Test
    @DisplayName("Should throw resource access exception when terminating process and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenTerminatingProcess() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> runtimeClient.terminateProcess(TEST_EXECUTION_ID));
    }

    @Test
    @DisplayName("Should move execution successfully when valid parameters are provided")
    void shouldMoveExecutionSuccessfullyWhenValidParametersAreProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ProcessModificationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() ->
                runtimeClient.moveExecution(TEST_EXECUTION_ID, "activity-123", "target-def-456"));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).body(any(ProcessModificationRequest.class));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should handle bad request when moving execution")
    void shouldHandleBadRequestWhenMovingExecution() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ProcessModificationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> runtimeClient.moveExecution(TEST_EXECUTION_ID, "activity-123", "target-def-456"));
    }

    @Test
    @DisplayName("Should throw server error when moving execution and server error occurs")
    void shouldThrowServerErrorWhenMovingExecution() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ProcessModificationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> runtimeClient.moveExecution(TEST_EXECUTION_ID, "activity-123", "target-def-456"));
    }

    @Test
    @DisplayName("Should throw resource access exception when moving execution and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenMovingExecution() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ProcessModificationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> runtimeClient.moveExecution(TEST_EXECUTION_ID, "activity-123", "target-def-456"));
    }

    @Test
    @DisplayName("Should correlate message successfully when valid parameters are provided")
    void shouldCorrelateMessageSuccessfullyWhenValidParametersAreProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(CorrelateMessageRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> runtimeClient.correlateMessage(
                TEST_MESSAGE, TEST_BUSINESS_KEY, TEST_CORRELATION_KEYS, TEST_VARIABLES));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).body(any(CorrelateMessageRequest.class));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should set variables successfully when valid parameters are provided")
    void shouldSetVariablesSuccessfullyWhenValidParametersAreProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> runtimeClient.setVariables(TEST_EXECUTION_ID, TEST_VARIABLES));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).body(TEST_VARIABLES);
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should set local variables successfully when valid parameters are provided")
    void shouldSetLocalVariablesSuccessfullyWhenValidParametersAreProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> runtimeClient.setVariablesLocal(TEST_EXECUTION_ID, TEST_VARIABLES));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).body(TEST_VARIABLES);
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should handle bad request exception when starting process with invalid parameters")
    void shouldHandleBadRequestExceptionWhenStartingProcessWithInvalidParameters() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(StartProcessByKeyRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When & Then
        assertThrows(HttpClientErrorException.class,
                () -> runtimeClient.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should throw server error exception when starting process and server error occurs")
    void shouldThrowServerErrorExceptionWhenStartingProcessAndServerErrorOccurs() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(StartProcessByKeyRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> runtimeClient.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should handle bad request exception when correlating message with invalid parameters")
    void shouldHandleBadRequestExceptionWhenCorrelatingMessageWithInvalidParameters() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(CorrelateMessageRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> runtimeClient.correlateMessage(TEST_MESSAGE, TEST_BUSINESS_KEY, TEST_CORRELATION_KEYS, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should throw server error exception when setting variables and server error occurs")
    void shouldThrowServerErrorExceptionWhenSettingVariablesAndServerErrorOccurs() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> runtimeClient.setVariables(TEST_EXECUTION_ID, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should throw resource access exception when operations and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenOperationsAndConnectionErrorOccurs() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(StartProcessByKeyRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class))
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> runtimeClient.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should find process successfully")
    void shouldFindProcessSuccessfully() {
        var expected = createTestProcess("p-find");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ProcessFilter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class)).thenReturn(expected);

        var result = runtimeClient.findProcess(ProcessFilter.builder().build());

        assertNotNull(result);
        assertEquals("p-find", result.id());
    }

    @Test
    @DisplayName("Should return null when findProcess returns NotFound")
    void shouldReturnNullWhenFindProcessNotFound() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ProcessFilter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Process.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        var result = runtimeClient.findProcess(ProcessFilter.builder().build());

        assertNull(result);
    }

    @Test
    @DisplayName("Should suspend process by id successfully")
    void shouldSuspendProcessByIdSuccessfully() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        assertDoesNotThrow(() -> runtimeClient.suspendProcessById(TEST_EXECUTION_ID));

        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should suspend processes by definition id successfully")
    void shouldSuspendProcessesByDefinitionIdSuccessfully() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        assertDoesNotThrow(() -> runtimeClient.suspendProcessesByDefinitionId(TEST_DEFINITION_ID));

        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should suspend processes by definition key successfully")
    void shouldSuspendProcessesByDefinitionKeySuccessfully() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        assertDoesNotThrow(() -> runtimeClient.suspendProcessesByDefinitionKey(TEST_DEFINITION_KEY));

        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should resume process by id successfully")
    void shouldResumeProcessByIdSuccessfully() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        assertDoesNotThrow(() -> runtimeClient.resumeProcessById(TEST_EXECUTION_ID));

        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should resume processes by definition id successfully")
    void shouldResumeProcessesByDefinitionIdSuccessfully() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        assertDoesNotThrow(() -> runtimeClient.resumeProcessesByDefinitionId(TEST_DEFINITION_ID));

        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should resume processes by definition key successfully")
    void shouldResumeProcessesByDefinitionKeySuccessfully() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        assertDoesNotThrow(() -> runtimeClient.resumeProcessesByDefinitionKey(TEST_DEFINITION_KEY));

        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should throw bad request when suspending process by id")
    void shouldThrowBadRequestWhenSuspendingProcessById() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        assertThrows(HttpClientErrorException.class,
                () -> runtimeClient.suspendProcessById(TEST_EXECUTION_ID));
    }

    @Test
    @DisplayName("Should throw server error when resuming process by id")
    void shouldThrowServerErrorWhenResumingProcessById() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        assertThrows(HttpServerErrorException.class,
                () -> runtimeClient.resumeProcessById(TEST_EXECUTION_ID));
    }

    @Test
    @DisplayName("Should throw resource access exception when suspending processes by definition key")
    void shouldThrowResourceAccessExceptionWhenSuspendingByDefinitionKey() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection error"));

        assertThrows(ResourceAccessException.class,
                () -> runtimeClient.suspendProcessesByDefinitionKey(TEST_DEFINITION_KEY));
    }

}