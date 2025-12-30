package com.leorces.engine;

import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.correlation.command.CorrelateMessageCommand;
import com.leorces.engine.process.command.MoveExecutionCommand;
import com.leorces.engine.process.command.ResumeProcessCommand;
import com.leorces.engine.process.command.SuspendProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.engine.service.process.ProcessRuntimeService;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuntimeServiceImpl Tests")
class RuntimeServiceImplTest {

    @Mock
    private ProcessRuntimeService processRuntimeService;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private RuntimeServiceImpl service;

    @Test
    @DisplayName("startProcessById delegates to ProcessRuntimeService")
    void startProcessByIdDelegates() {
        // Given
        var definitionId = "def1";
        var businessKey = "bk1";
        Map<String, Object> vars = Map.of("a", 1);
        var process = mock(Process.class);
        when(processRuntimeService.startByDefinitionId(definitionId, businessKey, vars)).thenReturn(process);

        // When
        var result = service.startProcessById(definitionId, businessKey, vars);

        // Then
        assertThat(result).isEqualTo(process);
        verify(processRuntimeService).startByDefinitionId(definitionId, businessKey, vars);
        verifyNoMoreInteractions(processRuntimeService);
    }

    @Test
    @DisplayName("startProcessByKey delegates to ProcessRuntimeService")
    void startProcessByKeyDelegates() {
        // Given
        var key = "key1";
        var businessKey = "bk1";
        Map<String, Object> vars = Map.of("a", 1);
        var process = mock(Process.class);
        when(processRuntimeService.startByDefinitionKey(key, businessKey, vars)).thenReturn(process);

        // When
        var result = service.startProcessByKey(key, businessKey, vars);

        // Then
        assertThat(result).isEqualTo(process);
        verify(processRuntimeService).startByDefinitionKey(key, businessKey, vars);
        verifyNoMoreInteractions(processRuntimeService);
    }

    @Test
    @DisplayName("setVariable delegates to dispatcher with correct command")
    void setVariableDelegates() {
        // Given
        var executionId = "exec1";
        var key = "var";
        var value = 42;

        // When
        service.setVariable(executionId, key, value);

        // Then
        var captor = ArgumentCaptor.forClass(SetVariablesCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        var command = captor.getValue();
        assertThat(command.executionId()).isEqualTo(executionId);
        assertThat(command.variables()).containsEntry(key, value);
        assertThat(command.local()).isFalse();
    }

    @Test
    @DisplayName("setVariableLocal delegates to dispatcher with local flag")
    void setVariableLocalDelegates() {
        // Given
        var executionId = "exec1";
        var key = "var";
        var value = 42;

        // When
        service.setVariableLocal(executionId, key, value);

        // Then
        var captor = ArgumentCaptor.forClass(SetVariablesCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        var command = captor.getValue();
        assertThat(command.executionId()).isEqualTo(executionId);
        assertThat(command.variables()).containsEntry(key, value);
        assertThat(command.local()).isTrue();
    }

    @Test
    @DisplayName("moveExecution delegates to dispatcher with correct command")
    void moveExecutionDelegates() {
        // Given
        var processId = "proc1";
        var activityId = "act1";
        var targetDefinitionId = "target1";

        // When
        service.moveExecution(processId, activityId, targetDefinitionId);

        // Then
        var captor = ArgumentCaptor.forClass(MoveExecutionCommand.class);
        verify(dispatcher).dispatch(captor.capture());

        var command = captor.getValue();
        assertThat(command.processId()).isEqualTo(processId);
        assertThat(command.activityId()).isEqualTo(activityId);
        assertThat(command.targetDefinitionId()).isEqualTo(targetDefinitionId);
    }

    @Test
    @DisplayName("correlateMessage delegates to dispatcher with correct command")
    void correlateMessageDelegates() {
        // Given
        var messageName = "msg1";
        var businessKey = "bk1";
        Map<String, Object> correlationKeys = Map.of("c", 1);
        Map<String, Object> processVariables = Map.of("p", 2);

        // When
        service.correlateMessage(messageName, businessKey, correlationKeys, processVariables);

        // Then
        var captor = ArgumentCaptor.forClass(CorrelateMessageCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        var command = captor.getValue();
        assertThat(command.messageName()).isEqualTo(messageName);
        assertThat(command.businessKey()).isEqualTo(businessKey);
        assertThat(command.correlationKeys()).isEqualTo(correlationKeys);
        assertThat(command.processVariables()).isEqualTo(processVariables);
    }

    @Test
    @DisplayName("terminateProcess dispatches TerminateProcessCommand")
    void terminateProcessDelegates() {
        var processId = "proc1";

        service.terminateProcess(processId);

        var captor = ArgumentCaptor.forClass(TerminateProcessCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().processId()).isEqualTo(processId);
    }

    @Test
    @DisplayName("resolveIncident dispatches RetryAllActivitiesCommand")
    void resolveIncidentDelegates() {
        var processId = "proc1";

        service.resolveIncident(processId);

        var captor = ArgumentCaptor.forClass(RetryAllActivitiesCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().processId()).isEqualTo(processId);
    }

    @Test
    @DisplayName("suspendProcessById dispatches async SuspendProcessCommand")
    void suspendProcessByIdDelegatesAsync() {
        var processId = "proc1";

        service.suspendProcessById(processId);

        var captor = ArgumentCaptor.forClass(SuspendProcessCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());
        assertThat(captor.getValue().processId()).isEqualTo(processId);
    }

    @Test
    @DisplayName("suspendProcessesByDefinitionId dispatches async SuspendProcessCommand")
    void suspendByDefinitionIdDelegatesAsync() {
        var definitionId = "def1";

        service.suspendProcessesByDefinitionId(definitionId);

        var captor = ArgumentCaptor.forClass(SuspendProcessCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());
        assertThat(captor.getValue().definitionId()).isEqualTo(definitionId);
    }

    @Test
    @DisplayName("suspendProcessesByDefinitionKey dispatches async SuspendProcessCommand")
    void suspendByDefinitionKeyDelegatesAsync() {
        var definitionKey = "key1";

        service.suspendProcessesByDefinitionKey(definitionKey);

        var captor = ArgumentCaptor.forClass(SuspendProcessCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());
        assertThat(captor.getValue().definitionKey()).isEqualTo(definitionKey);
    }

    @Test
    @DisplayName("resumeProcessById dispatches async ResumeProcessCommand")
    void resumeProcessByIdDelegatesAsync() {
        var processId = "proc1";

        service.resumeProcessById(processId);

        var captor = ArgumentCaptor.forClass(ResumeProcessCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());
        assertThat(captor.getValue().processId()).isEqualTo(processId);
    }

    @Test
    @DisplayName("resumeProcessesByDefinitionId dispatches async ResumeProcessCommand")
    void resumeByDefinitionIdDelegatesAsync() {
        var definitionId = "def1";

        service.resumeProcessesByDefinitionId(definitionId);

        var captor = ArgumentCaptor.forClass(ResumeProcessCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());
        assertThat(captor.getValue().definitionId()).isEqualTo(definitionId);
    }

    @Test
    @DisplayName("resumeProcessesByDefinitionKey dispatches async ResumeProcessCommand")
    void resumeByDefinitionKeyDelegatesAsync() {
        var definitionKey = "key1";

        service.resumeProcessesByDefinitionKey(definitionKey);

        var captor = ArgumentCaptor.forClass(ResumeProcessCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());
        assertThat(captor.getValue().definitionKey()).isEqualTo(definitionKey);
    }

    @Test
    @DisplayName("findProcess delegates to ProcessRuntimeService")
    void findProcessDelegates() {
        var filter = mock(ProcessFilter.class);
        var process = mock(Process.class);
        when(processRuntimeService.find(filter)).thenReturn(process);

        var result = service.findProcess(filter);

        assertThat(result).isEqualTo(process);
        verify(processRuntimeService).find(filter);
        verifyNoMoreInteractions(processRuntimeService);
    }

    @Test
    @DisplayName("correlateMessage with businessKey only delegates correctly")
    void correlateMessageWithBusinessKeyOnly() {
        var messageName = "msg1";
        var businessKey = "bk1";

        service.correlateMessage(messageName, businessKey);

        var captor = ArgumentCaptor.forClass(CorrelateMessageCommand.class);
        verify(dispatcher).dispatch(captor.capture());

        var command = captor.getValue();
        assertThat(command.messageName()).isEqualTo(messageName);
        assertThat(command.businessKey()).isEqualTo(businessKey);
        assertThat(command.correlationKeys()).isEmpty();
        assertThat(command.processVariables()).isEmpty();
    }

    @Test
    @DisplayName("correlateMessage with correlation keys only delegates correctly")
    void correlateMessageWithCorrelationKeysOnly() {
        var messageName = "msg1";
        Map<String, Object> keys = Map.of("k", 1);

        service.correlateMessage(messageName, keys);

        var captor = ArgumentCaptor.forClass(CorrelateMessageCommand.class);
        verify(dispatcher).dispatch(captor.capture());

        var command = captor.getValue();
        assertThat(command.messageName()).isEqualTo(messageName);
        assertThat(command.businessKey()).isNull();
        assertThat(command.correlationKeys()).isEqualTo(keys);
    }

}
