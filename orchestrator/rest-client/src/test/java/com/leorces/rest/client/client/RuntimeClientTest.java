package com.leorces.rest.client.client;

import com.leorces.model.runtime.process.Process;
import com.leorces.rest.client.model.request.CorrelateMessageRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        // When
        var result = runtimeClient.startProcessByKey(TEST_DEFINITION_KEY, TEST_BUSINESS_KEY, TEST_VARIABLES);

        // Then
        assertNull(result);
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
        assertDoesNotThrow(() -> runtimeClient.correlateMessage(
                TEST_MESSAGE, TEST_BUSINESS_KEY, TEST_CORRELATION_KEYS, TEST_VARIABLES));
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
}