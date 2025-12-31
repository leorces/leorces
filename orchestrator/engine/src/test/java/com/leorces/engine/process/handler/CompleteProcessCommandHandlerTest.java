package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.engine.service.process.ProcessMetrics;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
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
@DisplayName("CompleteProcessCommandHandler Tests")
class CompleteProcessCommandHandlerTest {

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ProcessMetrics processMetrics;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private CompleteProcessCommandHandler handler;

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
    @DisplayName("Handle should complete process and record metrics")
    void handleShouldCompleteProcessAndRecordMetrics() {
        var command = CompleteProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));
        when(activityPersistence.isAllCompleted(process.id())).thenReturn(true);

        handler.handle(command);

        verify(processPersistence).complete(process.id());
        verify(processMetrics).recordProcessCompletedMetric(process);
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Handle should dispatch CompleteActivityCommand for call activity")
    void handleShouldDispatchCompleteActivityCommandForCallActivity() {
        process = Process.builder()
                .id("process-1")
                .parentId("parent-1")
                .businessKey("bk1")
                .definition(null)
                .state(ProcessState.ACTIVE)
                .variables(List.of())
                .build();

        var command = CompleteProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));
        when(activityPersistence.isAllCompleted(process.id())).thenReturn(true);

        handler.handle(command);

        verify(processPersistence).complete(process.id());
        verify(processMetrics).recordProcessCompletedMetric(process);

        ArgumentCaptor<CompleteActivityCommand> captor = ArgumentCaptor.forClass(CompleteActivityCommand.class);
        verify(dispatcher).dispatchAsync(captor.capture());

        assertThat(captor.getValue().activityId()).isEqualTo(process.id());
        assertThat(captor.getValue().variables()).isEmpty();
    }

    @Test
    @DisplayName("Handle should do nothing if process is not active")
    void handleShouldDoNothingIfProcessNotActive() {
        process = Process.builder()
                .id("process-1")
                .businessKey("bk1")
                .definition(null)
                .state(ProcessState.COMPLETED)
                .variables(List.of())
                .build();

        var command = CompleteProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));

        handler.handle(command);

        verifyNoInteractions(activityPersistence, processMetrics, dispatcher);
        verify(processPersistence, never()).complete(process.id());
    }

    @Test
    @DisplayName("Handle should do nothing if not all activities are completed")
    void handleShouldDoNothingIfNotAllActivitiesCompleted() {
        var command = CompleteProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));
        when(activityPersistence.isAllCompleted(process.id())).thenReturn(false);

        handler.handle(command);

        verify(processPersistence, never()).complete(process.id());
        verify(processMetrics, never()).recordProcessCompletedMetric(process);
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Handle should throw ExecutionException if process not found")
    void handleShouldThrowExceptionIfProcessNotFound() {
        var command = CompleteProcessCommand.of("unknown-id");
        when(processPersistence.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class);

        verifyNoInteractions(activityPersistence, processMetrics, dispatcher);
    }

}
