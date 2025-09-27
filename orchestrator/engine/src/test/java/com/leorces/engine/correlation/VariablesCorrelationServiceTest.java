package com.leorces.engine.correlation;


import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.correlation.CorrelateVariablesEvent;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ConditionalActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;


@DisplayName("VariablesCorrelationService Unit Tests")
@ExtendWith(MockitoExtension.class)
class VariablesCorrelationServiceTest {

    private static final String PROCESS_ID = "test-process-id";
    private static final String EXECUTION_ID = "execution-id";
    private static final String EXECUTION_ID_2 = "execution-id-2";
    private static final String ACTIVITY_ID = "activity-id";
    private static final String ACTIVITY_ID_2 = "activity-id-2";
    private static final String CONDITION = "${variable1 == 'test' && variable2 > 10}";
    private static final String FALSE_CONDITION = "${variable1 == 'wrong'}";
    private static final String VARIABLE_NAME_1 = "variable1";
    private static final String VARIABLE_NAME_2 = "variable2";
    private static final String VARIABLE_VALUE_1 = "test";
    private static final String VARIABLE_VALUE_2 = "15";
    private static final LocalDateTime TEST_TIME = LocalDateTime.now();

    @Mock
    private VariableRuntimeService variableRuntimeService;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private EngineEventBus eventBus;

    @Mock
    private Process process;

    @Mock
    private ProcessDefinition processDefinition;

    @Mock
    private ConditionalActivityDefinition conditionalActivity1;

    @Mock
    private ConditionalActivityDefinition conditionalActivity2;

    @Mock
    private ActivityDefinition regularActivity;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    private VariablesCorrelationService variablesCorrelationService;
    private Variable variable1;
    private Variable variable2;

    @BeforeEach
    void setUp() {
        variablesCorrelationService = new VariablesCorrelationService(
                variableRuntimeService,
                expressionEvaluator,
                eventBus
        );

        variable1 = createVariable("var1-id", EXECUTION_ID, VARIABLE_NAME_1, VARIABLE_VALUE_1);
        variable2 = createVariable("var2-id", EXECUTION_ID, VARIABLE_NAME_2, VARIABLE_VALUE_2);
    }

    @Test
    @DisplayName("Should handle variables event and correlate successfully")
    void shouldHandleVariablesEventAndCorrelateSuccessfully() {
        //Given
        var variables = List.of(variable1, variable2);
        var correlateVariablesEvent = new CorrelateVariablesEvent(process, variables, this);
        var activities = List.of((ActivityDefinition) conditionalActivity1);
        var scope = List.of(EXECUTION_ID);
        var variablesMap = createVariablesMap();

        setupSuccessfulCorrelation(activities, scope, variablesMap);

        //When
        variablesCorrelationService.handleVariables(correlateVariablesEvent);

        //Then
        verify(expressionEvaluator).evaluateBoolean(CONDITION, variablesMap);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should return early when variables list is empty")
    void shouldReturnEarlyWhenVariablesListIsEmpty() {
        //Given
        var emptyVariables = List.<Variable>of();
        var correlateVariablesEvent = new CorrelateVariablesEvent(process, emptyVariables, this);

        //When
        variablesCorrelationService.handleVariables(correlateVariablesEvent);

        //Then
        verify(process, never()).definition();
        verify(eventBus, never()).publish(any());
        verify(expressionEvaluator, never()).evaluateBoolean(any(), anyMap());
    }

    @Test
    @DisplayName("Should trigger activity when condition is met")
    void shouldTriggerActivityWhenConditionIsMet() {
        //Given
        var variables = List.of(variable1, variable2);
        var activities = List.of((ActivityDefinition) conditionalActivity1);
        var scope = List.of(EXECUTION_ID);
        var variablesMap = createVariablesMap();

        setupSuccessfulCorrelation(activities, scope, variablesMap);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(ActivityEvent.class);
    }

    @Test
    @DisplayName("Should not trigger activity when condition is not met")
    void shouldNotTriggerActivityWhenConditionIsNotMet() {
        //Given
        var variables = List.of(variable1, variable2);
        var activities = List.of((ActivityDefinition) conditionalActivity1);
        var scope = List.of(EXECUTION_ID);
        var variablesMap = createVariablesMap();

        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, FALSE_CONDITION, ACTIVITY_ID);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(scope);
        when(variableRuntimeService.toMap(any())).thenReturn(variablesMap);
        when(expressionEvaluator.evaluateBoolean(FALSE_CONDITION, variablesMap)).thenReturn(false);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should filter only conditional activities from process definition")
    void shouldFilterOnlyConditionalActivitiesFromProcessDefinition() {
        //Given
        var variables = List.of(variable1);
        var activities = List.of(conditionalActivity1, regularActivity, conditionalActivity2);
        var scope = List.of(EXECUTION_ID);
        var variablesMap = createVariablesMapSingle();

        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, CONDITION, ACTIVITY_ID);
        setupConditionalActivity(conditionalActivity2, CONDITION, ACTIVITY_ID_2);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(scope);
        when(processDefinition.scope(ACTIVITY_ID_2)).thenReturn(scope);
        when(variableRuntimeService.toMap(any())).thenReturn(variablesMap);
        when(expressionEvaluator.evaluateBoolean(CONDITION, variablesMap)).thenReturn(true);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus, times(2)).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should trigger multiple activities when multiple conditions are met")
    void shouldTriggerMultipleActivitiesWhenMultipleConditionsAreMet() {
        //Given
        var variables = List.of(variable1, variable2);
        var activities = List.of(conditionalActivity1, (ActivityDefinition) conditionalActivity2);
        var scope = List.of(EXECUTION_ID);
        var variablesMap = createVariablesMap();

        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, CONDITION, ACTIVITY_ID);
        setupConditionalActivity(conditionalActivity2, CONDITION, ACTIVITY_ID_2);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(scope);
        when(processDefinition.scope(ACTIVITY_ID_2)).thenReturn(scope);
        when(variableRuntimeService.toMap(any())).thenReturn(variablesMap);
        when(expressionEvaluator.evaluateBoolean(CONDITION, variablesMap)).thenReturn(true);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus, times(2)).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle variables from multiple execution contexts")
    void shouldHandleVariablesFromMultipleExecutionContexts() {
        //Given
        var variable3 = createVariable("var3-id", EXECUTION_ID_2, "variable3", "value3");
        var variables = List.of(variable1, variable2, variable3);
        var activities = List.of((ActivityDefinition) conditionalActivity1);
        var scope = List.of(EXECUTION_ID, EXECUTION_ID_2);
        var variablesMap = createVariablesMapMultiple();

        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, CONDITION, ACTIVITY_ID);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(scope);
        when(variableRuntimeService.toMap(any())).thenReturn(variablesMap);
        when(expressionEvaluator.evaluateBoolean(CONDITION, variablesMap)).thenReturn(true);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus).publish(any(ActivityEvent.class));
        verify(variableRuntimeService).toMap(any());
    }

    @Test
    @DisplayName("Should handle empty scope correctly")
    void shouldHandleEmptyScopeCorrectly() {
        //Given
        var variables = List.of(variable1);
        var activities = List.of((ActivityDefinition) conditionalActivity1);
        var emptyScope = List.<String>of();
        var emptyVariablesMap = new HashMap<String, Object>();

        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, CONDITION, ACTIVITY_ID);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(emptyScope);
        when(variableRuntimeService.toMap(List.of())).thenReturn(emptyVariablesMap);
        when(expressionEvaluator.evaluateBoolean(CONDITION, emptyVariablesMap)).thenReturn(false);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus, never()).publish(any());
        verify(variableRuntimeService).toMap(List.of());
    }

    @Test
    @DisplayName("Should handle variables not in scope")
    void shouldHandleVariablesNotInScope() {
        //Given
        var variables = List.of(variable1);
        var activities = List.of((ActivityDefinition) conditionalActivity1);
        var scope = List.of("different-execution-id");
        var emptyVariablesMap = new HashMap<String, Object>();

        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, CONDITION, ACTIVITY_ID);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(scope);
        when(variableRuntimeService.toMap(List.of())).thenReturn(emptyVariablesMap);
        when(expressionEvaluator.evaluateBoolean(CONDITION, emptyVariablesMap)).thenReturn(false);

        //When
        variablesCorrelationService.handleVariables(new CorrelateVariablesEvent(process, variables, this));

        //Then
        verify(eventBus, never()).publish(any());
        verify(variableRuntimeService).toMap(List.of());
    }

    private void setupSuccessfulCorrelation(List<ActivityDefinition> activities, List<String> scope, Map<String, Object> variablesMap) {
        setupProcess(activities);
        setupConditionalActivity(conditionalActivity1, CONDITION, ACTIVITY_ID);
        when(processDefinition.scope(ACTIVITY_ID)).thenReturn(scope);
        when(variableRuntimeService.toMap(any())).thenReturn(variablesMap);
        when(expressionEvaluator.evaluateBoolean(CONDITION, variablesMap)).thenReturn(true);
    }

    private void setupProcess(List<ActivityDefinition> activities) {
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.activities()).thenReturn(activities);
    }

    private void setupConditionalActivity(ConditionalActivityDefinition activity, String condition, String activityId) {
        when(activity.condition()).thenReturn(condition);
        when(activity.id()).thenReturn(activityId);
    }

    private Variable createVariable(String id, String executionDefinitionId, String varKey, String varValue) {
        return Variable.builder()
                .id(id)
                .processId(PROCESS_ID)
                .executionId("exec-" + id)
                .executionDefinitionId(executionDefinitionId)
                .varKey(varKey)
                .varValue(varValue)
                .type("string")
                .createdAt(TEST_TIME)
                .updatedAt(TEST_TIME)
                .build();
    }

    private Map<String, Object> createVariablesMap() {
        var map = new HashMap<String, Object>();
        map.put(VARIABLE_NAME_1, VARIABLE_VALUE_1);
        map.put(VARIABLE_NAME_2, VARIABLE_VALUE_2);
        return map;
    }

    private Map<String, Object> createVariablesMapSingle() {
        var map = new HashMap<String, Object>();
        map.put(VARIABLE_NAME_1, VARIABLE_VALUE_1);
        return map;
    }

    private Map<String, Object> createVariablesMapMultiple() {
        var map = new HashMap<String, Object>();
        map.put(VARIABLE_NAME_1, VARIABLE_VALUE_1);
        map.put(VARIABLE_NAME_2, VARIABLE_VALUE_2);
        map.put("variable3", "value3");
        return map;
    }

}