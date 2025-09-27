package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.behaviour.FailableActivityBehavior;
import com.leorces.engine.event.activity.fail.FailActivityByIdEventAsync;
import com.leorces.engine.event.activity.fail.FailActivityEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivitiesEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityByIdEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityEventAsync;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityFailService Tests")
class ActivityFailServiceTest {

    private static final String ACTIVITY_ID = "test-activity-id";
    private static final String PROCESS_ID = "test-process-id";
    private static final String DEFINITION_ID = "test-definition-id";
    private static final String VARIABLE_KEY = "testVar";
    private static final String VARIABLE_VALUE = "testValue";

    @Mock
    private VariableRuntimeService variableRuntimeService;

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private TaskExecutorService taskService;

    @Mock
    private FailableActivityBehavior failableBehavior;

    private ActivityFailService activityFailService;

    @BeforeEach
    void setUp() {
        activityFailService = new ActivityFailService(
                variableRuntimeService,
                behaviorResolver,
                activityFactory,
                taskService
        );
    }

    @Test
    @DisplayName("Should handle fail activity event successfully")
    void shouldHandleFailActivityEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new FailActivityEventAsync(activity);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleFail(event);

        // Then
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(variableRuntimeService).setProcessVariables(activity.process(), Map.of());
        verify(failableBehavior).fail(activity);
    }

    @Test
    @DisplayName("Should handle fail activity by ID event with variables successfully")
    void shouldHandleFailActivityByIdEventWithVariablesSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var variables = createVariablesMap();
        var event = new FailActivityByIdEventAsync(ACTIVITY_ID, variables);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleFail(event);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(variableRuntimeService).setProcessVariables(activity.process(), variables);
        verify(failableBehavior).fail(activity);
    }

    @Test
    @DisplayName("Should handle retry activity by ID event successfully")
    void shouldHandleRetryActivityByIdEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new RetryActivityByIdEventAsync(ACTIVITY_ID);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleRetry(event);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(failableBehavior).retry(activity);
    }

    @Test
    @DisplayName("Should handle retry activity event successfully")
    void shouldHandleRetryActivityEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new RetryActivityEventAsync(activity);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleRetry(event);

        // Then
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(failableBehavior).retry(activity);
    }

    @Test
    @DisplayName("Should handle retry activities event successfully")
    void shouldHandleRetryActivitiesEventSuccessfully() {
        // Given
        var activity1 = createActivityExecution();
        var activity2 = createActivityExecution();
        var activities = List.of(activity1, activity2);
        var event = new RetryActivitiesEventAsync(activities);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run(); // Execute the runnable synchronously for testing
            return null;
        }).when(taskService).execute(any(Runnable.class));

        // When
        activityFailService.handleRetry(event);

        // Then
        verify(taskService, times(2)).execute(any(Runnable.class));
        verify(behaviorResolver, times(2)).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(failableBehavior, times(2)).retry(any(ActivityExecution.class));
    }

    @Test
    @DisplayName("Should handle fail activity with empty variables")
    void shouldHandleFailActivityWithEmptyVariables() {
        // Given
        var activity = createActivityExecution();
        var event = new FailActivityEventAsync(activity);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleFail(event);

        // Then
        verify(variableRuntimeService).setProcessVariables(activity.process(), Map.of());
        verify(failableBehavior).fail(activity);
    }

    @Test
    @DisplayName("Should handle fail activity with custom variables")
    void shouldHandleFailActivityWithCustomVariables() {
        // Given
        var activity = createActivityExecution();
        var variables = createVariablesMap();
        var event = new FailActivityByIdEventAsync(ACTIVITY_ID, variables);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleFail(event);

        // Then
        verify(variableRuntimeService).setProcessVariables(activity.process(), variables);
        verify(failableBehavior).fail(activity);
    }

    @Test
    @DisplayName("Should handle failable strategy not found gracefully")
    void shouldHandleFailableStrategyNotFoundGracefully() {
        // Given
        var activity = createActivityExecution();
        var event = new FailActivityEventAsync(activity);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.empty());

        // When
        activityFailService.handleFail(event);

        // Then
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(variableRuntimeService).setProcessVariables(activity.process(), Map.of());
        verify(failableBehavior, never()).fail(any());
    }

    @Test
    @DisplayName("Should handle retry with failable strategy not found gracefully")
    void shouldHandleRetryWithFailableStrategyNotFoundGracefully() {
        // Given
        var activity = createActivityExecution();
        var event = new RetryActivityEventAsync(activity);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.empty());

        // When
        activityFailService.handleRetry(event);

        // Then
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(failableBehavior, never()).retry(any());
    }

    @Test
    @DisplayName("Should handle empty activities list in retry gracefully")
    void shouldHandleEmptyActivitiesListInRetryGracefully() {
        // Given
        var emptyActivities = List.<ActivityExecution>of();
        var event = new RetryActivitiesEventAsync(emptyActivities);

        // When
        activityFailService.handleRetry(event);

        // Then
        verify(taskService, never()).execute(any(Runnable.class));
        verify(behaviorResolver, never()).resolveFailableStrategy(any());
        verify(failableBehavior, never()).retry(any());
    }

    @Test
    @DisplayName("Should call retry method directly for public retry")
    void shouldCallRetryMethodDirectlyForPublicRetry() {
        // Given
        var activity = createActivityExecution();

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.retry(activity);

        // Then
        verify(behaviorResolver).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
        verify(failableBehavior).retry(activity);
    }

    @Test
    @DisplayName("Should handle null variables in fail event")
    void shouldHandleNullVariablesInFailEvent() {
        // Given
        var activity = createActivityExecution();
        var event = new FailActivityByIdEventAsync(ACTIVITY_ID, null);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleFail(event);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(variableRuntimeService).setProcessVariables(activity.process(), null);
        verify(failableBehavior).fail(activity);
    }

    @Test
    @DisplayName("Should verify correct activity types are passed to behavior resolver")
    void shouldVerifyCorrectActivityTypesArePassedToBehaviorResolver() {
        // Given
        var activity = createActivityExecution();
        var failEvent = new FailActivityEventAsync(activity);
        var retryEvent = new RetryActivityEventAsync(activity);

        when(behaviorResolver.resolveFailableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(failableBehavior));

        // When
        activityFailService.handleFail(failEvent);
        activityFailService.handleRetry(retryEvent);

        // Then
        verify(behaviorResolver, times(2)).resolveFailableStrategy(ActivityType.EXTERNAL_TASK);
    }

    private ActivityExecution createActivityExecution() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.FAILED);
        when(mockActivity.process()).thenReturn(process);
        return mockActivity;
    }

    private Process createProcess() {
        var mockProcess = mock(Process.class, withSettings().lenient());
        when(mockProcess.id()).thenReturn(PROCESS_ID);
        return mockProcess;
    }

    private Map<String, Object> createVariablesMap() {
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_KEY, VARIABLE_VALUE);
        return variables;
    }

}