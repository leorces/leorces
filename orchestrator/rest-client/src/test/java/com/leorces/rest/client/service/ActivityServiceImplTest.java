package com.leorces.rest.client.service;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.rest.client.client.ActivityClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Activity Service Implementation Tests")
class ActivityServiceImplTest {

    private static final String ACTIVITY_DEFINITION_ID = "test-activity-def-123";
    private static final String PROCESS_ID = "test-process-456";
    private static final String ACTIVITY_ID = "test-activity-789";
    private static final String TOPIC = "test-topic";
    private static final String PROCESS_DEFINITION_KEY = "test-process-key";
    private static final int LIMIT = 10;
    private static final Map<String, Object> VARIABLES = Map.of("key1", "value1", "key2", 42);
    private static final Map<String, Object> EMPTY_VARIABLES = Map.of();

    @Mock
    private ActivityClient activityClient;

    @InjectMocks
    private ActivityServiceImpl activityService;

    @Test
    @DisplayName("Should run activity with definition ID and process ID")
    void shouldRunActivityWithDefinitionIdAndProcessId() {
        //When
        activityService.run(ACTIVITY_DEFINITION_ID, PROCESS_ID);

        //Then
        verify(activityClient).run(PROCESS_ID, ACTIVITY_DEFINITION_ID);
    }

    @Test
    @DisplayName("Should complete activity without variables")
    void shouldCompleteActivityWithoutVariables() {
        //When
        activityService.complete(ACTIVITY_ID);

        //Then
        verify(activityClient).complete(ACTIVITY_ID, EMPTY_VARIABLES);
    }

    @Test
    @DisplayName("Should complete activity with variables")
    void shouldCompleteActivityWithVariables() {
        //When
        activityService.complete(ACTIVITY_ID, VARIABLES);

        //Then
        verify(activityClient).complete(ACTIVITY_ID, VARIABLES);
    }

    @Test
    @DisplayName("Should fail activity without variables")
    void shouldFailActivityWithoutVariables() {
        //When
        activityService.fail(ACTIVITY_ID);

        //Then
        verify(activityClient).fail(ACTIVITY_ID, EMPTY_VARIABLES);
    }

    @Test
    @DisplayName("Should fail activity with variables")
    void shouldFailActivityWithVariables() {
        //When
        activityService.fail(ACTIVITY_ID, VARIABLES);

        //Then
        verify(activityClient).fail(ACTIVITY_ID, VARIABLES);
    }

    @Test
    @DisplayName("Should terminate activity")
    void shouldTerminateActivity() {
        //When
        activityService.terminate(ACTIVITY_ID);

        //Then
        verify(activityClient).terminate(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should retry activity")
    void shouldRetryActivity() {
        //When
        activityService.retry(ACTIVITY_ID);

        //Then
        verify(activityClient).retry(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should poll activities and return result")
    void shouldPollActivitiesAndReturnResult() {
        //Given
        var expectedActivities = List.of(
                createActivity("activity-1"),
                createActivity("activity-2")
        );
        when(activityClient.poll(PROCESS_DEFINITION_KEY, TOPIC, LIMIT))
                .thenReturn(expectedActivities);

        //When
        var result = activityService.poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT);

        //Then
        verify(activityClient).poll(PROCESS_DEFINITION_KEY, TOPIC, LIMIT);
        assertThat(result).isEqualTo(expectedActivities);
    }

    private Activity createActivity(String id) {
        return Activity.builder()
                .id(id)
                .build();
    }
}