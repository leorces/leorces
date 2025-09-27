package com.leorces.engine;


import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.complete.CompleteActivityByIdEventAsync;
import com.leorces.engine.event.activity.fail.FailActivityByIdEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityByIdEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionIdAsync;
import com.leorces.engine.event.activity.terminate.TerminateActivityByIdAsync;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("ActivityServiceImpl Unit Tests")
@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    private static final String ACTIVITY_DEFINITION_ID = "test-activity-definition-id";
    private static final String PROCESS_ID = "test-process-id";
    private static final String ACTIVITY_ID = "test-activity-id";
    private static final String TOPIC = "test-topic";
    private static final String PROCESS_DEFINITION_KEY = "test-process-definition-key";
    private static final int LIMIT = 10;
    private static final Map<String, Object> VARIABLES = Map.of(
            "key1", "value1",
            "key2", 42,
            "key3", true
    );

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private EngineEventBus eventBus;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    private ActivityServiceImpl activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityServiceImpl(activityPersistence, eventBus);
    }

    @Test
    @DisplayName("Should run activity by definition id and process id")
    void shouldRunActivityByDefinitionIdAndProcessId() {
        //When
        activityService.run(ACTIVITY_DEFINITION_ID, PROCESS_ID);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(RunActivityByDefinitionIdAsync.class);

        var runEvent = (RunActivityByDefinitionIdAsync) capturedEvent;
        assertThat(runEvent.definitionId).isEqualTo(ACTIVITY_DEFINITION_ID);
        assertThat(runEvent.processId).isEqualTo(PROCESS_ID);
    }

    @Test
    @DisplayName("Should complete activity by id without variables")
    void shouldCompleteActivityByIdWithoutVariables() {
        //When
        activityService.complete(ACTIVITY_ID);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CompleteActivityByIdEventAsync.class);

        var completeEvent = (CompleteActivityByIdEventAsync) capturedEvent;
        assertThat(completeEvent.activityId).isEqualTo(ACTIVITY_ID);
        assertThat(completeEvent.variables).isEmpty();
    }

    @Test
    @DisplayName("Should complete activity by id with variables")
    void shouldCompleteActivityByIdWithVariables() {
        //When
        activityService.complete(ACTIVITY_ID, VARIABLES);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CompleteActivityByIdEventAsync.class);

        var completeEvent = (CompleteActivityByIdEventAsync) capturedEvent;
        assertThat(completeEvent.activityId).isEqualTo(ACTIVITY_ID);
        assertThat(completeEvent.variables).isEqualTo(VARIABLES);
    }

    @Test
    @DisplayName("Should fail activity by id without variables")
    void shouldFailActivityByIdWithoutVariables() {
        //When
        activityService.fail(ACTIVITY_ID);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(FailActivityByIdEventAsync.class);

        var failEvent = (FailActivityByIdEventAsync) capturedEvent;
        assertThat(failEvent.activityId).isEqualTo(ACTIVITY_ID);
        assertThat(failEvent.variables).isEmpty();
    }

    @Test
    @DisplayName("Should fail activity by id with variables")
    void shouldFailActivityByIdWithVariables() {
        //When
        activityService.fail(ACTIVITY_ID, VARIABLES);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(FailActivityByIdEventAsync.class);

        var failEvent = (FailActivityByIdEventAsync) capturedEvent;
        assertThat(failEvent.activityId).isEqualTo(ACTIVITY_ID);
        assertThat(failEvent.variables).isEqualTo(VARIABLES);
    }

    @Test
    @DisplayName("Should terminate activity by id")
    void shouldTerminateActivityById() {
        //When
        activityService.terminate(ACTIVITY_ID);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(TerminateActivityByIdAsync.class);

        var terminateEvent = (TerminateActivityByIdAsync) capturedEvent;
        assertThat(terminateEvent.activityId).isEqualTo(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should retry activity by id")
    void shouldRetryActivityById() {
        //When
        activityService.retry(ACTIVITY_ID);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(RetryActivityByIdEventAsync.class);

        var retryEvent = (RetryActivityByIdEventAsync) capturedEvent;
        assertThat(retryEvent.activityId).isEqualTo(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should poll activities from persistence")
    void shouldPollActivitiesFromPersistence() {
        //Given
        var expectedActivities = List.of(
                createTestActivity("activity-1"),
                createTestActivity("activity-2")
        );
        when(activityPersistence.poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT))
                .thenReturn(expectedActivities);

        //When
        var result = activityService.poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedActivities);
        verify(activityPersistence).poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT);
    }

    @Test
    @DisplayName("Should return empty list when no activities available for polling")
    void shouldReturnEmptyListWhenNoActivitiesAvailableForPolling() {
        //Given
        var emptyList = List.<Activity>of();
        when(activityPersistence.poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT))
                .thenReturn(emptyList);

        //When
        var result = activityService.poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(activityPersistence).poll(TOPIC, PROCESS_DEFINITION_KEY, LIMIT);
    }

    private Activity createTestActivity(String activityId) {
        var testVariables = List.of(
                Variable.builder()
                        .id("var-1")
                        .processId(PROCESS_ID)
                        .executionId(activityId)
                        .executionDefinitionId(ACTIVITY_DEFINITION_ID)
                        .varKey("key1")
                        .varValue("value1")
                        .type("STRING")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Variable.builder()
                        .id("var-2")
                        .processId(PROCESS_ID)
                        .executionId(activityId)
                        .executionDefinitionId(ACTIVITY_DEFINITION_ID)
                        .varKey("key2")
                        .varValue("42")
                        .type("INTEGER")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        return Activity.builder()
                .id(activityId)
                .definitionId(ACTIVITY_DEFINITION_ID)
                .variables(testVariables)
                .state(ActivityState.ACTIVE)
                .retries(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();
    }

}