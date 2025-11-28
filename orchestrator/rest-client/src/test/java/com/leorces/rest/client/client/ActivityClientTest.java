package com.leorces.rest.client.client;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.model.runtime.activity.ActivityState;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityClient Tests")
class ActivityClientTest {

    private static final String TEST_PROCESS_ID = "test-process-id";
    private static final String TEST_ACTIVITY_DEFINITION_ID = "test-activity-definition-id";
    private static final String TEST_ACTIVITY_ID = "test-activity-id";
    private static final String TEST_PROCESS_DEFINITION_KEY = "test-process-definition-key";
    private static final String TEST_TOPIC = "test-topic";
    private static final int TEST_LIMIT = 10;
    private static final Map<String, Object> TEST_VARIABLES = Map.of("key", "value");

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ActivityClient activityClient;

    @BeforeEach
    void setUp() {
        activityClient = new ActivityClient(restClient);
    }

    @Test
    @DisplayName("Should run activity successfully when valid parameters are provided")
    void shouldRunActivitySuccessfullyWhenValidParametersAreProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> activityClient.run(TEST_PROCESS_ID, TEST_ACTIVITY_DEFINITION_ID));

        // Then
        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should handle bad request exception when running activity with invalid parameters")
    void shouldHandleBadRequestExceptionWhenRunningActivityWithInvalidParameters() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> activityClient.run(TEST_PROCESS_ID, TEST_ACTIVITY_DEFINITION_ID));
    }

    @Test
    @DisplayName("Should throw server error exception when running activity and server error occurs")
    void shouldThrowServerErrorExceptionWhenRunningActivityAndServerErrorOccurs() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> activityClient.run(TEST_PROCESS_ID, TEST_ACTIVITY_DEFINITION_ID));
    }

    @Test
    @DisplayName("Should complete activity successfully when valid parameters are provided")
    void shouldCompleteActivitySuccessfullyWhenValidParametersAreProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_VARIABLES)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> activityClient.complete(TEST_ACTIVITY_ID, TEST_VARIABLES));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).body(TEST_VARIABLES);
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should fail activity successfully when valid parameters are provided")
    void shouldFailActivitySuccessfullyWhenValidParametersAreProvided() {
        // Given
        var failure = ActivityFailure.of("Failure reason");
        var request = new FailActivityRequest(failure, TEST_VARIABLES);

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> activityClient.fail(TEST_ACTIVITY_ID, failure, TEST_VARIABLES));

        // Then
        verify(restClient).put();
        verify(requestBodySpec).body(request);
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should terminate activity successfully when valid activity id is provided")
    void shouldTerminateActivitySuccessfullyWhenValidActivityIdIsProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> activityClient.terminate(TEST_ACTIVITY_ID));

        // Then
        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should retry activity successfully when valid activity id is provided")
    void shouldRetryActivitySuccessfullyWhenValidActivityIdIsProvided() {
        // Given
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> activityClient.retry(TEST_ACTIVITY_ID));

        // Then
        verify(restClient).put();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should poll activities successfully when valid parameters are provided")
    void shouldPollActivitiesSuccessfullyWhenValidParametersAreProvided() {
        // Given
        var expectedActivities = List.of(createTestActivity("activity1"), createTestActivity("activity2"));
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedActivities);

        // When
        var result = activityClient.poll(TEST_PROCESS_DEFINITION_KEY, TEST_TOPIC, TEST_LIMIT);

        // Then
        assertNotNull(result);
        assertEquals(expectedActivities.size(), result.size());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return empty list when polling activities and not found exception occurs")
    void shouldReturnEmptyListWhenPollingActivitiesAndNotFoundExceptionOccurs() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        // When
        var result = activityClient.poll(TEST_PROCESS_DEFINITION_KEY, TEST_TOPIC, TEST_LIMIT);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw resource access exception when polling activities and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenPollingActivitiesAndConnectionErrorOccurs() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> activityClient.poll(TEST_PROCESS_DEFINITION_KEY, TEST_TOPIC, TEST_LIMIT));
    }

    private Activity createTestActivity(String activityId) {
        return Activity.builder()
                .id(activityId)
                .definitionId("test-definition-id")
                .variables(Collections.emptyList())
                .state(ActivityState.SCHEDULED)
                .retries(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(null)
                .completedAt(null)
                .build();
    }

}