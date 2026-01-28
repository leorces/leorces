package com.leorces.engine.job.migration.handler;

import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.migration.command.SingleProcessMigrationCommand;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.process.ProcessExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SingleProcessMigrationCommandHandler Tests")
class SingleProcessMigrationCommandHandlerTest {

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private SingleProcessMigrationCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(SingleProcessMigrationCommand.class);
    }

    @Test
    @DisplayName("should do nothing if instructions are empty")
    void shouldDoNothingIfInstructionsEmpty() {
        // Given
        var process = mock(ProcessExecution.class);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of())
                .build();
        var command = SingleProcessMigrationCommand.of(process, migration);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(dispatcher, process);
    }

    @Test
    @DisplayName("should handle migration instructions for active activities")
    void shouldHandleInstructions() {
        // Given
        var processId = "proc-1";
        var fromActDefId = "from-act";
        var toActDefId = "to-act";
        var actId = "act-1";

        var process = mock(ProcessExecution.class);
        when(process.id()).thenReturn(processId);

        var activity = mock(Activity.class);
        when(activity.id()).thenReturn(actId);
        when(activity.isInTerminalState()).thenReturn(false);

        when(process.getActivitiesByDefinitionId(fromActDefId)).thenReturn(List.of(activity));

        var instruction = new ActivityMigrationInstruction(fromActDefId, toActDefId);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of(instruction))
                .build();
        var command = SingleProcessMigrationCommand.of(process, migration);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(DeleteActivityCommand.of(actId));
        verify(dispatcher).dispatch(RunActivityCommand.of(toActDefId, processId));
    }

    @Test
    @DisplayName("should ignore completed activities")
    void shouldIgnoreCompletedActivities() {
        // Given
        var fromActDefId = "from-act";
        var toActDefId = "to-act";

        var process = mock(ProcessExecution.class);
        var activity = mock(Activity.class);
        when(activity.isInTerminalState()).thenReturn(true);

        when(process.getActivitiesByDefinitionId(fromActDefId)).thenReturn(List.of(activity));

        var instruction = new ActivityMigrationInstruction(fromActDefId, toActDefId);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of(instruction))
                .build();
        var command = SingleProcessMigrationCommand.of(process, migration);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(dispatcher);
    }

}
