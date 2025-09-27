package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.activity.complete.CompleteActivityAsync;
import com.leorces.engine.event.activity.complete.CompleteActivityByDefinitionIdEventAsync;
import com.leorces.engine.event.activity.complete.CompleteActivityByIdEventAsync;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityCompleteService Tests")
class ActivityCompleteServiceTest {

    private static final String ACTIVITY_ID = "test-activity-id";
    private static final String DEFINITION_ID = "test-definition-id";
    private static final String PROCESS_ID = "test-process-id";
    private static final String VARIABLE_KEY = "testVar";
    private static final String VARIABLE_VALUE = "testValue";

    @Mock
    private VariableRuntimeService variableRuntimeService;

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private EngineEventBus eventBus;

    @Mock
    private ActivityBehavior activityBehavior;

    private ActivityCompleteService activityCompleteService;

    @BeforeEach
    void setUp() {
        activityCompleteService = new ActivityCompleteService(
                variableRuntimeService,
                behaviorResolver,
                activityFactory,
                eventBus
        );
    }

    @Test
    @DisplayName("Should handle complete activity async event successfully")
    void shouldHandleCompleteActivityAsyncEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new CompleteActivityAsync(activity);
        var completedActivity = createCompletedActivityExecution();

        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenReturn(completedActivity);
        when(variableRuntimeService.evaluate(completedActivity, Map.of())).thenReturn(List.of());
        when(variableRuntimeService.toMap(List.of())).thenReturn(Map.of());

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activity);
        verify(variableRuntimeService).setProcessVariables(completedActivity.process(), Map.of());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle complete activity by ID event successfully")
    void shouldHandleCompleteActivityByIdEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var variables = createVariablesMap();
        var event = new CompleteActivityByIdEventAsync(ACTIVITY_ID, variables);
        var completedActivity = createCompletedActivityExecution();
        var outputVariables = List.of(createVariable());
        var outputVariablesMap = createVariablesMap();

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenReturn(completedActivity);
        when(variableRuntimeService.evaluate(completedActivity, Map.of())).thenReturn(outputVariables);
        when(variableRuntimeService.toMap(outputVariables)).thenReturn(outputVariablesMap);

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activity);
        verify(variableRuntimeService).evaluate(completedActivity, Map.of());
        verify(variableRuntimeService).toMap(outputVariables);
        verify(variableRuntimeService).setProcessVariables(eq(completedActivity.process()), any(Map.class));
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle complete activity by definition ID event successfully")
    void shouldHandleCompleteActivityByDefinitionIdEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new CompleteActivityByDefinitionIdEventAsync(DEFINITION_ID, PROCESS_ID);
        var completedActivity = createCompletedActivityExecution();

        when(activityFactory.getByDefinitionId(DEFINITION_ID, PROCESS_ID)).thenReturn(activity);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenReturn(completedActivity);
        when(variableRuntimeService.evaluate(completedActivity, Map.of())).thenReturn(List.of());
        when(variableRuntimeService.toMap(List.of())).thenReturn(Map.of());

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(activityFactory).getByDefinitionId(DEFINITION_ID, PROCESS_ID);
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activity);
        verify(variableRuntimeService).setProcessVariables(completedActivity.process(), Map.of());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle activity completion with output variables")
    void shouldHandleActivityCompletionWithOutputVariables() {
        // Given
        var activity = createActivityExecutionWithOutputs();
        var event = new CompleteActivityAsync(activity);
        var completedActivity = createCompletedActivityExecutionWithOutputs();
        var outputVariables = List.of(createVariable());
        var outputVariablesMap = createVariablesMap();

        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenReturn(completedActivity);
        when(variableRuntimeService.evaluate(completedActivity, outputVariablesMap)).thenReturn(outputVariables);
        when(variableRuntimeService.toMap(outputVariables)).thenReturn(outputVariablesMap);

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(variableRuntimeService).evaluate(completedActivity, outputVariablesMap);
        verify(variableRuntimeService).toMap(outputVariables);
        verify(variableRuntimeService).setProcessVariables(completedActivity.process(), outputVariablesMap);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle activity completion failure and publish fail event")
    void shouldHandleActivityCompletionFailureAndPublishFailEvent() {
        // Given
        var activity = createActivityExecution();
        var event = new CompleteActivityAsync(activity);
        var exception = new RuntimeException("Completion failed");

        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenThrow(exception);

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activity);
        verify(variableRuntimeService, never()).setProcessVariables(any(), any());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should merge input variables with output variables correctly")
    void shouldMergeInputVariablesWithOutputVariablesCorrectly() {
        // Given
        var activity = createActivityExecution();
        var inputVariables = Map.<String, Object>of("input1", "value1", "input2", "value2");
        var event = new CompleteActivityByIdEventAsync(ACTIVITY_ID, inputVariables);
        var completedActivity = createCompletedActivityExecution();
        var outputVariables = List.of(createVariable());
        var outputVariablesMap = Map.<String, Object>of(VARIABLE_KEY, VARIABLE_VALUE);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenReturn(completedActivity);
        when(variableRuntimeService.evaluate(completedActivity, Map.of())).thenReturn(outputVariables);
        when(variableRuntimeService.toMap(outputVariables)).thenReturn(outputVariablesMap);

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(variableRuntimeService).setProcessVariables(eq(completedActivity.process()), argThat(variables ->
                variables.containsKey("input1") &&
                        variables.containsKey("input2") &&
                        variables.containsKey(VARIABLE_KEY) &&
                        variables.get("input1").equals("value1") &&
                        variables.get("input2").equals("value2") &&
                        variables.get(VARIABLE_KEY).equals(VARIABLE_VALUE)
        ));
    }

    @Test
    @DisplayName("Should handle empty variables gracefully")
    void shouldHandleEmptyVariablesGracefully() {
        // Given
        var activity = createActivityExecution();
        var event = new CompleteActivityAsync(activity);
        var completedActivity = createCompletedActivityExecution();

        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activity)).thenReturn(completedActivity);
        when(variableRuntimeService.evaluate(completedActivity, Map.of())).thenReturn(List.of());
        when(variableRuntimeService.toMap(List.of())).thenReturn(Map.of());

        // When
        activityCompleteService.handleComplete(event);

        // Then
        verify(variableRuntimeService).setProcessVariables(completedActivity.process(), Map.of());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    private ActivityExecution createActivityExecution() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.outputs()).thenReturn(Map.of());
        return mockActivity;
    }

    private ActivityExecution createActivityExecutionWithOutputs() {
        var activity = createActivityExecution();
        when(activity.outputs()).thenReturn(createVariablesMap());
        return activity;
    }

    private ActivityExecution createCompletedActivityExecution() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.COMPLETED);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.outputs()).thenReturn(Map.of());
        return mockActivity;
    }

    private ActivityExecution createCompletedActivityExecutionWithOutputs() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.COMPLETED);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.outputs()).thenReturn(createVariablesMap());
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

    private Variable createVariable() {
        return Variable.builder()
                .varKey(VARIABLE_KEY)
                .varValue(VARIABLE_VALUE)
                .build();
    }

}