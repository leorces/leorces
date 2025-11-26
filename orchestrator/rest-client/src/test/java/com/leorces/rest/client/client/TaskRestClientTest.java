package com.leorces.rest.client.client;

import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.model.ExternalTask;
import com.leorces.rest.client.model.request.FailActivityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskRestClient Tests")
class TaskRestClientTest {

    private static final String TEST_TASK_ID = "test-task-id";
    private static final String TEST_TOPIC = "test-topic";
    private static final String TEST_PROCESS_DEFINITION_KEY = "test-process-definition-key";
    private static final int TEST_SIZE = 5;
    private static final Map<String, Object> TEST_VARIABLES = Map.of("key", "value");

    @Mock
    private RestClient restClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    private TaskRestClient taskRestClient;

    private static ExternalTask createTestTask(String id) {
        // Using mock since ExternalTask might be complex
        return mock(ExternalTask.class);
    }

    @BeforeEach
    void setUp() {
        taskRestClient = new TaskRestClient(restClient);
    }

    @Test
    @DisplayName("Should complete task successfully when valid parameters are provided")
    void shouldCompleteTaskSuccessfullyWhenValidParametersAreProvided() {
        // Given
        var expectedResponse = ResponseEntity.ok().<Void>build();
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(expectedResponse);

        // When
        var result = taskRestClient.complete(TEST_TASK_ID, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restClient).put();
        verify(requestBodySpec).body(TEST_VARIABLES);
    }

    @Test
    @DisplayName("Should fail task successfully when valid parameters are provided")
    void shouldFailTaskSuccessfullyWhenValidParametersAreProvided() {
        // Given
        var failure = ActivityFailure.of("Failure reason");
        var request = new FailActivityRequest(failure, TEST_VARIABLES);
        var expectedResponse = ResponseEntity.ok().<Void>build();
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(expectedResponse);

        // When
        var result = taskRestClient.fail(TEST_TASK_ID, failure, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restClient).put();
        verify(requestBodySpec).body(request);
    }

    @Test
    @DisplayName("Should poll tasks successfully when valid parameters are provided")
    void shouldPollTasksSuccessfullyWhenValidParametersAreProvided() {
        // Given
        var expectedTasks = List.of(createTestTask("task1"), createTestTask("task2"));
        var expectedResponse = ResponseEntity.ok(expectedTasks);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(expectedResponse);

        // When
        var result = taskRestClient.poll(TEST_TOPIC, TEST_PROCESS_DEFINITION_KEY, TEST_SIZE);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return bad request response when completing task with bad request")
    void shouldReturnBadRequestResponseWhenCompletingTaskWithBadRequest() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = taskRestClient.complete(TEST_TASK_ID, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found response when completing non-existent task")
    void shouldReturnNotFoundResponseWhenCompletingNonExistentTask() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        // When
        var result = taskRestClient.complete(TEST_TASK_ID, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("Should return conflict response when completing task with conflict")
    void shouldReturnConflictResponseWhenCompletingTaskWithConflict() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null));

        // When
        var result = taskRestClient.complete(TEST_TASK_ID, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
    }

    @Test
    @DisplayName("Should throw server error exception when completing task and server error occurs")
    void shouldThrowServerErrorExceptionWhenCompletingTaskAndServerErrorOccurs() {
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
                () -> taskRestClient.complete(TEST_TASK_ID, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should return bad request response when failing task with bad request")
    void shouldReturnBadRequestResponseWhenFailingTaskWithBadRequest() {
        // Given
        var failure = ActivityFailure.of("Failure reason");
        var request = new FailActivityRequest(failure, TEST_VARIABLES);
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = taskRestClient.fail(TEST_TASK_ID, failure, TEST_VARIABLES);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Should throw resource access exception when completing task and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenCompletingTaskAndConnectionErrorOccurs() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> taskRestClient.complete(TEST_TASK_ID, TEST_VARIABLES));
    }

    @Test
    @DisplayName("Should return empty list when polling tasks and not found exception occurs")
    void shouldReturnEmptyListWhenPollingTasksAndNotFoundExceptionOccurs() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        // When
        var result = taskRestClient.poll(TEST_TOPIC, TEST_PROCESS_DEFINITION_KEY, TEST_SIZE);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

}