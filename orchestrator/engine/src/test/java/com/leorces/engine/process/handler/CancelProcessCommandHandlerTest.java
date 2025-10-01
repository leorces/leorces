package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.CancelAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.engine.process.ProcessMetrics;
import com.leorces.engine.process.command.CancelProcessCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
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
@DisplayName("CancelProcessCommandHandler Tests")
class CancelProcessCommandHandlerTest {

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ProcessMetrics processMetrics;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private CancelProcessCommandHandler handler;

    private Process process;

    @BeforeEach
    void setUp() {
        process = Process.builder()
                .id("process-1")
                .businessKey("bk1")
                .definition(null)
                .state(null)
                .variables(List.of())
                .build();
    }

    @Test
    @DisplayName("Handle should cancel process and dispatch CancelAllActivitiesCommand")
    void handleShouldCancelProcessAndDispatchCancelAllActivities() {
        // Given
        var command = CancelProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));

        var activity1 = mock(ActivityExecution.class);
        var activity2 = mock(ActivityExecution.class);
        var activeActivities = List.of(activity1, activity2);
        when(activityPersistence.findActive(process.id())).thenReturn(activeActivities);

        // When
        handler.handle(command);

        // Then
        verify(activityPersistence).findActive(process.id());

        var captor = ArgumentCaptor.forClass(CancelAllActivitiesCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().activities()).isEqualTo(activeActivities);

        verify(processPersistence).cancel(process);
        verify(processMetrics).recordProcessCancelledMetric(process);
    }

    @Test
    @DisplayName("Handle should throw ProcessNotFoundException when process does not exist")
    void handleShouldThrowExceptionWhenProcessNotFound() {
        // Given
        var command = CancelProcessCommand.of("unknown-id");
        when(processPersistence.findById("unknown-id")).thenReturn(Optional.empty());

        // When & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ProcessNotFoundException.class);

        verifyNoInteractions(activityPersistence, dispatcher, processMetrics);
    }

    @Test
    @DisplayName("Handle should work correctly when there are no active activities")
    void handleShouldWorkWhenNoActiveActivities() {
        // Given
        var command = CancelProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));
        when(activityPersistence.findActive(process.id())).thenReturn(List.of());

        // When
        handler.handle(command);

        // Then
        var captor = ArgumentCaptor.forClass(CancelAllActivitiesCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().activities()).isEmpty();

        verify(processPersistence).cancel(process);
        verify(processMetrics).recordProcessCancelledMetric(process);
    }

    @Test
    @DisplayName("Handle should dispatch only once even if multiple activities")
    void handleShouldDispatchOnlyOnce() {
        // Given
        var command = CancelProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));
        when(activityPersistence.findActive(process.id()))
                .thenReturn(List.of(mock(ActivityExecution.class), mock(ActivityExecution.class), mock(ActivityExecution.class)));

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, times(1)).dispatch(any(CancelAllActivitiesCommand.class));
    }

    @Test
    @DisplayName("Handle should cancel process even if no active activities")
    void handleShouldCancelProcessEvenIfNoActiveActivities() {
        // Given
        var command = CancelProcessCommand.of(process.id());
        when(processPersistence.findById(process.id())).thenReturn(Optional.of(process));
        when(activityPersistence.findActive(process.id())).thenReturn(List.of());

        // When
        handler.handle(command);

        // Then
        verify(processPersistence).cancel(process);
        verify(processMetrics).recordProcessCancelledMetric(process);
    }

}
