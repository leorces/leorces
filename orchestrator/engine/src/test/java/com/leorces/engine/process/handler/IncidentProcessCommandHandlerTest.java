package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.engine.service.process.ProcessMetrics;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncidentProcessCommandHandler Tests")
class IncidentProcessCommandHandlerTest {

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ProcessMetrics processMetrics;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private IncidentProcessCommandHandler handler;

    private Process process;

    @BeforeEach
    void setUp() {
        process = Process.builder()
                .id("process-1")
                .businessKey("bk1")
                .definition(null)
                .state(ProcessState.ACTIVE)
                .variables(List.of())
                .build();
    }

    @Test
    @DisplayName("Handle should mark process as incident and record metrics")
    void handleShouldMarkProcessAsIncidentAndRecordMetrics() {
        var command = new IncidentProcessCommand(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));

        handler.handle(command);

        verify(processPersistence).incident(process.id());
        verify(processMetrics).recordProcessIncidentMetric(process);
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Handle should dispatch FailActivityCommand if process is call activity")
    void handleShouldDispatchFailActivityCommandForCallActivity() {
        process = Process.builder()
                .id("process-1")
                .parentId("parent-1")
                .businessKey("bk1")
                .definition(null)
                .state(ProcessState.ACTIVE)
                .variables(List.of())
                .build();

        var command = new IncidentProcessCommand(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));

        handler.handle(command);

        verify(processPersistence).incident(process.id());
        verify(processMetrics).recordProcessIncidentMetric(process);

        ArgumentCaptor<FailActivityCommand> captor = ArgumentCaptor.forClass(FailActivityCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());

        assertThat(captor.getValue().activityId()).isEqualTo(process.id());
    }

    @Test
    @DisplayName("Handle should do nothing if process is already in INCIDENT state")
    void handleShouldDoNothingIfProcessAlreadyInIncidentState() {
        process = Process.builder()
                .id("process-1")
                .businessKey("bk1")
                .definition(null)
                .state(ProcessState.INCIDENT)
                .variables(List.of())
                .build();

        var command = new IncidentProcessCommand(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));

        handler.handle(command);

        verifyNoInteractions(processMetrics, dispatcher);
        verify(processPersistence, never()).incident(process.id());
    }

    @Test
    @DisplayName("Handle should throw ExecutionException if process not found")
    void handleShouldThrowExceptionIfProcessNotFound() {
        var command = new IncidentProcessCommand("unknown-id");
        when(processPersistence.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class);

        verifyNoInteractions(processMetrics, dispatcher);
    }

}
