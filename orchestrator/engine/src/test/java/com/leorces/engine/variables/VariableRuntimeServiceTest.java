package com.leorces.engine.variables;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.VariablePersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariableRuntimeServiceTest {

    private static final String PROCESS_ID = "process123";
    private static final String ACTIVITY_ID = "activity456";
    private static final String EXECUTION_ID = "execution789";
    private static final String VARIABLE_NAME = "testVar";
    private static final String VARIABLE_VALUE = "testValue";

    @Mock
    private VariableEvaluationService evaluationService;

    @Mock
    private VariablesUpdateService updateService;

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private VariablePersistence variablePersistence;

    @InjectMocks
    private VariableRuntimeService variableRuntimeService;

    @Test
    @DisplayName("Should get scoped variables for activity")
    void shouldGetScopedVariablesForActivity() {
        // Given
        var activity = createActivityExecution();
        var scope = List.of(ACTIVITY_ID);
        var variables = List.of(createVariable());
        var expectedMap = new HashMap<String, Object>();
        expectedMap.put(VARIABLE_NAME, VARIABLE_VALUE);

        when(variablePersistence.findAll(PROCESS_ID, scope)).thenReturn(variables);
        when(variablesMapper.toMap(variables, scope)).thenReturn(expectedMap);

        // When
        var result = variableRuntimeService.getScopedVariables(activity);

        // Then
        assertThat(result).isEqualTo(expectedMap);
        verify(variablePersistence).findAll(PROCESS_ID, scope);
        verify(variablesMapper).toMap(variables, scope);
    }

    @Test
    @DisplayName("Should convert variables map to list")
    void shouldConvertVariablesMapToList() {
        // Given
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        var expectedList = List.of(createVariable());

        when(variablesMapper.map(variables)).thenReturn(expectedList);

        // When
        var result = variableRuntimeService.toList(variables);

        // Then
        assertThat(result).isEqualTo(expectedList);
        verify(variablesMapper).map(variables);
    }

    @Test
    @DisplayName("Should convert variables list to map")
    void shouldConvertVariablesListToMap() {
        // Given
        var variables = List.of(createVariable());
        var expectedMap = new HashMap<String, Object>();
        expectedMap.put(VARIABLE_NAME, VARIABLE_VALUE);

        when(variablesMapper.toMap(variables)).thenReturn(expectedMap);

        // When
        var result = variableRuntimeService.toMap(variables);

        // Then
        assertThat(result).isEqualTo(expectedMap);
        verify(variablesMapper).toMap(variables);
    }

    @Test
    @DisplayName("Should evaluate variables using evaluation service")
    void shouldEvaluateVariablesUsingEvaluationService() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        var expectedList = List.of(createVariable());

        when(evaluationService.evaluate(activity, variables)).thenReturn(expectedList);

        // When
        var result = variableRuntimeService.evaluate(activity, variables);

        // Then
        assertThat(result).isEqualTo(expectedList);
        verify(evaluationService).evaluate(activity, variables);
    }

    @Test
    @DisplayName("Should set process variables using update service")
    void shouldSetProcessVariablesUsingUpdateService() {
        // Given
        var process = createProcess();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);

        // When
        variableRuntimeService.setProcessVariables(process, variables);

        // Then
        verify(updateService).setProcessVariables(process, variables);
    }

    @Test
    @DisplayName("Should set variables using update service")
    void shouldSetVariablesUsingUpdateService() {
        // Given
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);

        // When
        variableRuntimeService.setVariables(EXECUTION_ID, variables);

        // Then
        verify(updateService).setVariables(EXECUTION_ID, variables);
    }

    @Test
    @DisplayName("Should set local variables using update service")
    void shouldSetLocalVariablesUsingUpdateService() {
        // Given
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);

        // When
        variableRuntimeService.setVariablesLocal(EXECUTION_ID, variables);

        // Then
        verify(updateService).setVariablesLocal(EXECUTION_ID, variables);
    }

    @Test
    @DisplayName("Should handle empty variables gracefully in all operations")
    void shouldHandleEmptyVariablesGracefullyInAllOperations() {
        // Given
        var activity = createActivityExecution();
        var process = createProcess();
        var emptyVariables = new HashMap<String, Object>();
        var emptyList = Collections.<Variable>emptyList();
        var emptyMap = Collections.<String, Object>emptyMap();
        var scope = List.of(ACTIVITY_ID);

        when(variablePersistence.findAll(PROCESS_ID, scope)).thenReturn(emptyList);
        when(variablesMapper.toMap(emptyList, scope)).thenReturn(emptyMap);
        when(variablesMapper.map(emptyVariables)).thenReturn(emptyList);
        when(variablesMapper.toMap(emptyList)).thenReturn(emptyMap);
        when(evaluationService.evaluate(activity, emptyVariables)).thenReturn(emptyList);

        // When & Then
        var scopedResult = variableRuntimeService.getScopedVariables(activity);
        assertThat(scopedResult).isEmpty();

        var listResult = variableRuntimeService.toList(emptyVariables);
        assertThat(listResult).isEmpty();

        var mapResult = variableRuntimeService.toMap(emptyList);
        assertThat(mapResult).isEmpty();

        var evaluateResult = variableRuntimeService.evaluate(activity, emptyVariables);
        assertThat(evaluateResult).isEmpty();

        variableRuntimeService.setProcessVariables(process, emptyVariables);
        variableRuntimeService.setVariables(EXECUTION_ID, emptyVariables);
        variableRuntimeService.setVariablesLocal(EXECUTION_ID, emptyVariables);

        // Verify all services were called
        verify(variablePersistence).findAll(PROCESS_ID, scope);
        verify(variablesMapper).toMap(emptyList, scope);
        verify(variablesMapper).map(emptyVariables);
        verify(variablesMapper).toMap(emptyList);
        verify(evaluationService).evaluate(activity, emptyVariables);
        verify(updateService).setProcessVariables(process, emptyVariables);
        verify(updateService).setVariables(EXECUTION_ID, emptyVariables);
        verify(updateService).setVariablesLocal(EXECUTION_ID, emptyVariables);
    }

    @Test
    @DisplayName("Should handle null inputs gracefully where applicable")
    void shouldHandleNullInputsGracefullyWhereApplicable() {
        // Given
        var activity = createActivityExecution();
        var process = createProcess();

        when(evaluationService.evaluate(eq(activity), isNull())).thenReturn(Collections.emptyList());

        // When & Then
        var evaluateResult = variableRuntimeService.evaluate(activity, null);
        assertThat(evaluateResult).isEmpty();

        variableRuntimeService.setProcessVariables(process, null);
        variableRuntimeService.setVariables(EXECUTION_ID, null);
        variableRuntimeService.setVariablesLocal(EXECUTION_ID, null);

        // Verify services were called
        verify(evaluationService).evaluate(activity, null);
        verify(updateService).setProcessVariables(process, null);
        verify(updateService).setVariables(EXECUTION_ID, null);
        verify(updateService).setVariablesLocal(EXECUTION_ID, null);
    }

    private ActivityExecution createActivityExecution() {
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.scope()).thenReturn(List.of(ACTIVITY_ID));
        return mockActivity;
    }

    private Process createProcess() {
        return Process.builder()
                .id(PROCESS_ID)
                .build();
    }

    private Variable createVariable() {
        return Variable.builder()
                .varKey(VARIABLE_NAME)
                .varValue(VARIABLE_VALUE)
                .build();
    }

}