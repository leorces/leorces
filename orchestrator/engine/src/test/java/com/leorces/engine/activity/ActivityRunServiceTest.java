package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.activity.run.RunActivitiesEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionIdAsync;
import com.leorces.engine.event.activity.run.RunActivityEventAsync;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.definition.activity.ActivityDefinition;
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
@DisplayName("ActivityRunService Tests")
class ActivityRunServiceTest {

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
    private TaskExecutorService taskService;

    @Mock
    private ActivityBehavior activityBehavior;

    private ActivityRunService activityRunService;

    @BeforeEach
    void setUp() {
        activityRunService = new ActivityRunService(
                variableRuntimeService,
                behaviorResolver,
                activityFactory,
                taskService
        );
    }

    @Test
    @DisplayName("Should handle run activity event successfully")
    void shouldHandleRunActivityEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityEventAsync(activity);
        var variables = List.of(createVariable());

        when(variableRuntimeService.evaluate(activity, activity.inputs())).thenReturn(variables);
        when(activity.toBuilder().variables(variables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(variableRuntimeService).evaluate(activity, activity.inputs());
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).run(activityWithVariables);
    }

    @Test
    @DisplayName("Should handle run activity by definition event successfully")
    void shouldHandleRunActivityByDefinitionEventSuccessfully() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();
        var activity = createActivityExecution();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityByDefinitionEventAsync(definition, process);
        var variables = List.of(createVariable());

        when(activityFactory.createActivity(definition, process)).thenReturn(activity);
        when(variableRuntimeService.evaluate(activity, activity.inputs())).thenReturn(variables);
        when(activity.toBuilder().variables(variables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(activityFactory).createActivity(definition, process);
        verify(variableRuntimeService).evaluate(activity, activity.inputs());
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).run(activityWithVariables);
    }

    @Test
    @DisplayName("Should handle run activity by definition ID async event successfully")
    void shouldHandleRunActivityByDefinitionIdAsyncEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityByDefinitionIdAsync(DEFINITION_ID, PROCESS_ID);
        var variables = List.of(createVariable());

        when(activityFactory.getNewByDefinitionId(DEFINITION_ID, PROCESS_ID)).thenReturn(activity);
        when(variableRuntimeService.evaluate(activity, activity.inputs())).thenReturn(variables);
        when(activity.toBuilder().variables(variables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(activityFactory).getNewByDefinitionId(DEFINITION_ID, PROCESS_ID);
        verify(variableRuntimeService).evaluate(activity, activity.inputs());
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).run(activityWithVariables);
    }

    @Test
    @DisplayName("Should handle run activities event successfully")
    void shouldHandleRunActivitiesEventSuccessfully() {
        // Given
        var definition1 = createActivityDefinition();
        var definition2 = createActivityDefinition();
        var definitions = List.of(definition1, definition2);
        var process = createProcess();
        var activity1 = createActivityExecution();
        var activity2 = createActivityExecution();
        var event = new RunActivitiesEventAsync(definitions, process);

        when(activityFactory.createActivity(definition1, process)).thenReturn(activity1);
        when(activityFactory.createActivity(definition2, process)).thenReturn(activity2);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(activityFactory).createActivity(definition1, process);
        verify(activityFactory).createActivity(definition2, process);
        verify(taskService, times(2)).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle run activity with input variables processing")
    void shouldHandleRunActivityWithInputVariablesProcessing() {
        // Given
        var activity = createActivityExecutionWithInputs();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityEventAsync(activity);
        var inputVariables = createVariablesMap();
        var evaluatedVariables = List.of(createVariable());

        when(variableRuntimeService.evaluate(activity, inputVariables)).thenReturn(evaluatedVariables);
        when(activity.toBuilder().variables(evaluatedVariables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(variableRuntimeService).evaluate(activity, inputVariables);
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).run(activityWithVariables);
    }

    @Test
    @DisplayName("Should handle run activity with empty input variables")
    void shouldHandleRunActivityWithEmptyInputVariables() {
        // Given
        var activity = createActivityExecution();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityEventAsync(activity);
        var emptyVariables = List.<Variable>of();

        when(variableRuntimeService.evaluate(activity, Map.of())).thenReturn(emptyVariables);
        when(activity.toBuilder().variables(emptyVariables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(variableRuntimeService).evaluate(activity, Map.of());
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).run(activityWithVariables);
    }

    @Test
    @DisplayName("Should handle empty activities list gracefully")
    void shouldHandleEmptyActivitiesListGracefully() {
        // Given
        var emptyDefinitions = List.<ActivityDefinition>of();
        var process = createProcess();
        var event = new RunActivitiesEventAsync(emptyDefinitions, process);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(activityFactory, never()).createActivity(any(), any());
        verify(taskService, never()).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Should process input variables before running activity")
    void shouldProcessInputVariablesBeforeRunningActivity() {
        // Given
        var activity = createActivityExecutionWithInputs();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityEventAsync(activity);
        var inputVariables = createVariablesMap();
        var evaluatedVariables = List.of(createVariable());

        when(variableRuntimeService.evaluate(activity, inputVariables)).thenReturn(evaluatedVariables);
        when(activity.toBuilder().variables(evaluatedVariables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        // Verify that variable processing happens before behavior resolution
        var inOrder = inOrder(variableRuntimeService, behaviorResolver, activityBehavior);
        inOrder.verify(variableRuntimeService).evaluate(activity, inputVariables);
        inOrder.verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        inOrder.verify(activityBehavior).run(activityWithVariables);
    }

    @Test
    @DisplayName("Should handle multiple activities with different definitions")
    void shouldHandleMultipleActivitiesWithDifferentDefinitions() {
        // Given
        var definition1 = createActivityDefinition();
        var definition2 = createActivityDefinitionWithId("different-definition-id");
        var definitions = List.of(definition1, definition2);
        var process = createProcess();
        var activity1 = createActivityExecution();
        var activity2 = createActivityExecutionWithId();
        var event = new RunActivitiesEventAsync(definitions, process);

        when(activityFactory.createActivity(definition1, process)).thenReturn(activity1);
        when(activityFactory.createActivity(definition2, process)).thenReturn(activity2);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(activityFactory).createActivity(definition1, process);
        verify(activityFactory).createActivity(definition2, process);
        verify(taskService, times(2)).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Should create activity with correct parameters for each definition")
    void shouldCreateActivityWithCorrectParametersForEachDefinition() {
        // Given
        var definition1 = createActivityDefinition();
        var definition2 = createActivityDefinitionWithId("another-definition-id");
        var definitions = List.of(definition1, definition2);
        var process = createProcess();
        var event = new RunActivitiesEventAsync(definitions, process);
        var activity = createActivityExecution();

        when(activityFactory.createActivity(any(), any())).thenReturn(activity);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(activityFactory).createActivity(definition1, process);
        verify(activityFactory).createActivity(definition2, process);
    }

    @Test
    @DisplayName("Should handle run activity event with null input variables")
    void shouldHandleRunActivityEventWithNullInputVariables() {
        // Given
        var activity = createActivityExecutionWithNullInputs();
        var activityWithVariables = createActivityExecutionWithVariables();
        var event = new RunActivityEventAsync(activity);
        var emptyVariables = List.<Variable>of();

        when(variableRuntimeService.evaluate(activity, null)).thenReturn(emptyVariables);
        when(activity.toBuilder().variables(emptyVariables).build()).thenReturn(activityWithVariables);
        when(behaviorResolver.resolveStrategy(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // When
        activityRunService.handleRun(event);

        // Then
        verify(variableRuntimeService).evaluate(activity, null);
        verify(behaviorResolver).resolveStrategy(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).run(activityWithVariables);
    }

    private ActivityDefinition createActivityDefinition() {
        var mockDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockDefinition.id()).thenReturn(DEFINITION_ID);
        when(mockDefinition.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        return mockDefinition;
    }

    private ActivityDefinition createActivityDefinitionWithId(String id) {
        var mockDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockDefinition.id()).thenReturn(id);
        when(mockDefinition.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        return mockDefinition;
    }

    private Process createProcess() {
        var mockProcess = mock(Process.class, withSettings().lenient());
        when(mockProcess.id()).thenReturn(PROCESS_ID);
        return mockProcess;
    }

    private ActivityExecution createActivityExecution() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        var mockBuilder = mock(ActivityExecution.ActivityExecutionBuilder.class, withSettings().lenient());
        var activityWithVariables = createActivityExecutionWithVariables();

        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.ACTIVE);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.inputs()).thenReturn(Map.of());
        when(mockActivity.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.variables(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(activityWithVariables);

        return mockActivity;
    }

    private ActivityExecution createActivityExecutionWithId() {
        var activity = createActivityExecution();
        when(activity.id()).thenReturn("different-activity-id");
        return activity;
    }

    private ActivityExecution createActivityExecutionWithInputs() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        var mockBuilder = mock(ActivityExecution.ActivityExecutionBuilder.class, withSettings().lenient());
        var activityWithVariables = createActivityExecutionWithVariables();

        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.ACTIVE);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.inputs()).thenReturn(createVariablesMap());
        when(mockActivity.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.variables(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(activityWithVariables);

        return mockActivity;
    }

    private ActivityExecution createActivityExecutionWithNullInputs() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        var mockBuilder = mock(ActivityExecution.ActivityExecutionBuilder.class, withSettings().lenient());
        var activityWithVariables = createActivityExecutionWithVariables();

        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.ACTIVE);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.inputs()).thenReturn(null);
        when(mockActivity.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.variables(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(activityWithVariables);

        return mockActivity;
    }

    private ActivityExecution createActivityExecutionWithVariables() {
        var process = createProcess();
        var variables = List.of(createVariable());
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.ACTIVE);
        when(mockActivity.process()).thenReturn(process);
        when(mockActivity.variables()).thenReturn(variables);
        when(mockActivity.inputs()).thenReturn(Map.of());
        return mockActivity;
    }

    private Variable createVariable() {
        return Variable.builder()
                .varKey(VARIABLE_KEY)
                .varValue(VARIABLE_VALUE)
                .build();
    }

    private Map<String, Object> createVariablesMap() {
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_KEY, VARIABLE_VALUE);
        return variables;
    }

}