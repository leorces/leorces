package com.leorces.rest.controller;

import com.leorces.api.ActivityService;
import com.leorces.model.runtime.activity.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityController Tests")
class ActivityControllerTest {

    private static final String TEST_PROCESS_ID = "test-process-id";
    private static final String TEST_ACTIVITY_DEFINITION_ID = "test-activity-definition-id";
    private static final String TEST_ACTIVITY_ID = "test-activity-id";
    private static final String TEST_PROCESS_DEFINITION_KEY = "test-process-definition-key";
    private static final String TEST_TOPIC = "test-topic";
    private static final int DEFAULT_POLL_SIZE = 10;
    private static final String TEST_VARIABLE_KEY = "testKey";
    private static final String TEST_VARIABLE_VALUE = "testValue";

    @Mock
    private ActivityService activityService;

    private ActivityController subject;

    @BeforeEach
    void setUp() {
        subject = new ActivityController(activityService);
    }

    @Test
    @DisplayName("Should run activity successfully")
    void shouldRunActivitySuccessfully() {
        // When
        subject.run(TEST_PROCESS_ID, TEST_ACTIVITY_DEFINITION_ID);

        // Then
        verify(activityService).run(TEST_ACTIVITY_DEFINITION_ID, TEST_PROCESS_ID);
    }

    @Test
    @DisplayName("Should complete activity with variables")
    void shouldCompleteActivityWithVariables() {
        // Given
        var variables = Map.<String, Object>of(TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE);

        // When
        subject.complete(TEST_ACTIVITY_ID, variables);

        // Then
        verify(activityService).complete(TEST_ACTIVITY_ID, variables);
    }

    @Test
    @DisplayName("Should complete activity with null variables")
    void shouldCompleteActivityWithNullVariables() {
        // When
        subject.complete(TEST_ACTIVITY_ID, null);

        // Then
        verify(activityService).complete(TEST_ACTIVITY_ID, Collections.emptyMap());
    }

    @Test
    @DisplayName("Should complete activity with empty variables")
    void shouldCompleteActivityWithEmptyVariables() {
        // Given
        var emptyVariables = Map.<String, Object>of();

        // When
        subject.complete(TEST_ACTIVITY_ID, emptyVariables);

        // Then
        verify(activityService).complete(TEST_ACTIVITY_ID, emptyVariables);
    }

    @Test
    @DisplayName("Should fail activity with variables")
    void shouldFailActivityWithVariables() {
        // Given
        var variables = Map.<String, Object>of(TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE);

        // When
        subject.fail(TEST_ACTIVITY_ID, variables);

        // Then
        verify(activityService).fail(TEST_ACTIVITY_ID, variables);
    }

    @Test
    @DisplayName("Should fail activity with null variables")
    void shouldFailActivityWithNullVariables() {
        // When
        subject.fail(TEST_ACTIVITY_ID, null);

        // Then
        verify(activityService).fail(TEST_ACTIVITY_ID, Collections.emptyMap());
    }

    @Test
    @DisplayName("Should terminate activity successfully")
    void shouldTerminateActivitySuccessfully() {
        // When
        subject.terminate(TEST_ACTIVITY_ID);

        // Then
        verify(activityService).terminate(TEST_ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should retry activity successfully")
    void shouldRetryActivitySuccessfully() {
        // When
        subject.retry(TEST_ACTIVITY_ID);

        // Then
        verify(activityService).retry(TEST_ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should poll activities successfully")
    void shouldPollActivitiesSuccessfully() {
        // Given
        var expectedActivities = createTestActivities();
        when(activityService.poll(TEST_TOPIC, TEST_PROCESS_DEFINITION_KEY, DEFAULT_POLL_SIZE))
                .thenReturn(expectedActivities);

        // When
        var result = subject.poll(TEST_PROCESS_DEFINITION_KEY, TEST_TOPIC, DEFAULT_POLL_SIZE).getBody();

        // Then
        assertThat(result).isEqualTo(expectedActivities);
        verify(activityService).poll(TEST_TOPIC, TEST_PROCESS_DEFINITION_KEY, DEFAULT_POLL_SIZE);
    }

    @Test
    @DisplayName("Should poll activities with custom size")
    void shouldPollActivitiesWithCustomSize() {
        // Given
        var customSize = 25;
        var expectedActivities = createTestActivities();
        when(activityService.poll(TEST_TOPIC, TEST_PROCESS_DEFINITION_KEY, customSize))
                .thenReturn(expectedActivities);

        // When
        var result = subject.poll(TEST_PROCESS_DEFINITION_KEY, TEST_TOPIC, customSize).getBody();

        // Then
        assertThat(result).isEqualTo(expectedActivities);
        verify(activityService).poll(TEST_TOPIC, TEST_PROCESS_DEFINITION_KEY, customSize);
    }

    @Test
    @DisplayName("Should handle activity run with empty strings")
    void shouldHandleActivityRunWithEmptyStrings() {
        // Given
        var emptyProcessId = "";
        var emptyActivityDefinitionId = "";

        // When
        subject.run(emptyProcessId, emptyActivityDefinitionId);

        // Then
        verify(activityService).run(emptyActivityDefinitionId, emptyProcessId);
    }

    @Test
    @DisplayName("Should handle complete with complex variable types")
    void shouldHandleCompleteWithComplexVariableTypes() {
        // Given
        var complexVariables = Map.of(
                "stringVar", "test",
                "numberVar", 42,
                "booleanVar", true,
                "listVar", List.of("item1", "item2")
        );

        // When
        subject.complete(TEST_ACTIVITY_ID, complexVariables);

        // Then
        verify(activityService).complete(TEST_ACTIVITY_ID, complexVariables);
    }

    @Test
    @DisplayName("Should handle fail with complex variable types")
    void shouldHandleFailWithComplexVariableTypes() {
        // Given
        var complexVariables = Map.<String, Object>of(
                "errorCode", "ERR_001",
                "timestamp", System.currentTimeMillis(),
                "retry", false
        );

        // When
        subject.fail(TEST_ACTIVITY_ID, complexVariables);

        // Then
        verify(activityService).fail(TEST_ACTIVITY_ID, complexVariables);
    }

    private List<Activity> createTestActivities() {
        return List.of();
    }

}