package com.leorces.engine.variables;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.correlation.CorrelationEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import com.leorces.persistence.VariablePersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariablesUpdateServiceTest {

    private static final String PROCESS_ID = "process123";
    private static final String ACTIVITY_ID = "activity456";
    private static final String EXECUTION_ID = "execution789";
    private static final String VARIABLE_NAME = "testVar";
    private static final String VARIABLE_VALUE = "testValue";
    private static final String NEW_VARIABLE_VALUE = "newValue";

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private VariablePersistence variablePersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private EngineEventBus eventBus;

    @InjectMocks
    private VariablesUpdateService variablesUpdateService;

    @Test
    @DisplayName("Should set variables for process when execution is process")
    void shouldSetVariablesForProcessWhenExecutionIsProcess() {
        // Given
        var process = createProcess();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        createVariable();
        var updatedVariable = createVariable().toBuilder().varValue(VARIABLE_VALUE).build();
        var mappedVariable = createVariable();

        when(processPersistence.findById(EXECUTION_ID)).thenReturn(Optional.of(process));
        when(variablesMapper.map(process, updatedVariable)).thenReturn(mappedVariable);
        when(variablesMapper.map(variables)).thenReturn(List.of(updatedVariable));
        when(variablePersistence.update(anyList())).thenReturn(List.of(mappedVariable));

        // When
        variablesUpdateService.setVariables(EXECUTION_ID, variables);

        // Then
        verify(processPersistence).findById(EXECUTION_ID);
        verify(variablePersistence).update(anyList());
        verify(eventBus).publish(any(CorrelationEvent.class));
        verify(activityPersistence, never()).findById(anyString());
    }

    @Test
    @DisplayName("Should set variables for activity when execution is activity")
    void shouldSetVariablesForActivityWhenExecutionIsActivity() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        createVariable();
        var updatedVariable = createVariable().toBuilder().varValue(VARIABLE_VALUE).build();
        var mappedVariable = createVariable();

        when(processPersistence.findById(EXECUTION_ID)).thenReturn(Optional.empty());
        when(activityPersistence.findById(EXECUTION_ID)).thenReturn(Optional.of(activity));
        when(variablesMapper.map(any(Process.class), any(Variable.class))).thenReturn(mappedVariable);
        when(variablesMapper.map(variables)).thenReturn(List.of(updatedVariable));
        when(variablePersistence.update(anyList())).thenReturn(List.of(mappedVariable));

        // When
        variablesUpdateService.setVariables(EXECUTION_ID, variables);

        // Then
        verify(processPersistence).findById(EXECUTION_ID);
        verify(activityPersistence).findById(EXECUTION_ID);
        verify(variablePersistence).update(anyList());
        verify(eventBus).publish(any(CorrelationEvent.class));
    }

    @Test
    @DisplayName("Should set local variables for activity only in current scope")
    void shouldSetLocalVariablesForActivityOnlyInCurrentScope() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        createVariable();
        var updatedVariable = createVariable().toBuilder().varValue(VARIABLE_VALUE).build();
        var mappedVariable = createVariable();

        when(processPersistence.findById(EXECUTION_ID)).thenReturn(Optional.empty());
        when(activityPersistence.findById(EXECUTION_ID)).thenReturn(Optional.of(activity));
        when(variablesMapper.map(any(Process.class), any(Variable.class))).thenReturn(mappedVariable);
        when(variablesMapper.map(variables)).thenReturn(List.of(updatedVariable));
        when(variablePersistence.update(anyList())).thenReturn(List.of(mappedVariable));

        // When
        variablesUpdateService.setVariablesLocal(EXECUTION_ID, variables);

        // Then
        verify(processPersistence).findById(EXECUTION_ID);
        verify(activityPersistence).findById(EXECUTION_ID);
        verify(variablePersistence).update(anyList());
        verify(eventBus).publish(any(CorrelationEvent.class));
    }

    @Test
    @DisplayName("Should set process variables and correlate")
    void shouldSetProcessVariablesAndCorrelate() {
        // Given
        var process = createProcess();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        var existingVariable = createVariable();
        var updatedVariable = createVariable().toBuilder().varValue(VARIABLE_VALUE).build();
        var mappedVariable = createVariable();

        when(variablesMapper.map(process, updatedVariable)).thenReturn(mappedVariable);
        when(variablesMapper.map(variables)).thenReturn(List.of(updatedVariable));
        when(variablePersistence.update(anyList())).thenReturn(List.of(mappedVariable));

        // When
        variablesUpdateService.setProcessVariables(process, variables);

        // Then
        verify(variablesMapper).map(variables);
        verify(variablePersistence).update(anyList());

        var eventCaptor = ArgumentCaptor.forClass(CorrelationEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
    }

    @Test
    @DisplayName("Should handle empty variables map gracefully")
    void shouldHandleEmptyVariablesMapGracefully() {
        // Given
        var process = createProcess();
        var variables = new HashMap<String, Object>();
        var emptyVariablesList = Collections.<Variable>emptyList();

        when(variablesMapper.map(variables)).thenReturn(emptyVariablesList);
        when(variablePersistence.update(emptyVariablesList)).thenReturn(emptyVariablesList);

        // When
        variablesUpdateService.setProcessVariables(process, variables);

        // Then
        verify(variablesMapper).map(variables);
        verify(variablePersistence).update(emptyVariablesList);
        verify(eventBus).publish(any(CorrelationEvent.class));
    }

    @Test
    @DisplayName("Should handle non-existing execution id gracefully")
    void shouldHandleNonExistingExecutionIdGracefully() {
        // Given
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);

        when(processPersistence.findById(EXECUTION_ID)).thenReturn(Optional.empty());
        when(activityPersistence.findById(EXECUTION_ID)).thenReturn(Optional.empty());

        // When
        variablesUpdateService.setVariables(EXECUTION_ID, variables);

        // Then
        verify(processPersistence).findById(EXECUTION_ID);
        verify(activityPersistence).findById(EXECUTION_ID);
        verify(variablePersistence, never()).update(anyList());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should merge new and existing variables correctly")
    void shouldMergeNewAndExistingVariablesCorrectly() {
        // Given
        var process = createProcess();
        var existingVariable = createVariable();
        process = process.toBuilder().variables(List.of(existingVariable)).build();

        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, NEW_VARIABLE_VALUE); // Update existing
        variables.put("newVar", "newValue"); // Add new

        var updatedExistingVariable = existingVariable.toBuilder().varValue(NEW_VARIABLE_VALUE).build();
        var newVariable = Variable.builder().varKey("newVar").varValue("newValue").build();
        var mappedUpdatedVariable = createVariable().toBuilder().varValue(NEW_VARIABLE_VALUE).build();
        var mappedNewVariable = Variable.builder().varKey("newVar").varValue("newValue").build();

        when(variablesMapper.map(variables)).thenReturn(List.of(updatedExistingVariable, newVariable));
        when(variablesMapper.map(eq(process), any(Variable.class))).thenReturn(mappedNewVariable);
        when(variablePersistence.update(anyList())).thenReturn(List.of(mappedUpdatedVariable, mappedNewVariable));

        // When
        variablesUpdateService.setProcessVariables(process, variables);

        // Then
        verify(variablesMapper).map(variables);
        verify(variablePersistence).update(anyList());
        verify(eventBus).publish(any(CorrelationEvent.class));
    }

    private Process createProcess() {
        return Process.builder()
                .id(PROCESS_ID)
                .variables(new ArrayList<>())
                .build();
    }

    private ActivityExecution createActivityExecution() {
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.processId()).thenReturn(PROCESS_ID);
        when(mockActivity.scope()).thenReturn(List.of(ACTIVITY_ID));
        when(mockActivity.variables()).thenReturn(new ArrayList<>());
        when(mockActivity.process()).thenReturn(createProcess());
        return mockActivity;
    }

    private Variable createVariable() {
        return Variable.builder()
                .varKey(VARIABLE_NAME)
                .varValue(VARIABLE_VALUE)
                .executionId(ACTIVITY_ID)
                .executionDefinitionId(ACTIVITY_ID)
                .build();
    }

}