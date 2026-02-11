package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.admin.migration.command.SingleProcessMigrationCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SingleProcessMigrationCommandHandler Tests")
class SingleProcessMigrationCommandHandlerTest {

    private static final String PROCESS_ID = "proc-1";
    private static final String FROM_ACTIVITY_ID = "from-act";
    private static final String TO_ACTIVITY_ID = "to-act";
    private static final String TO_DEFINITION_ID = "to-def-id";
    private static final String ACTIVITY_ID = "act-1";

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private com.leorces.persistence.ProcessPersistence processPersistence;

    @InjectMocks
    private SingleProcessMigrationCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(SingleProcessMigrationCommand.class);
    }

    @Test
    @DisplayName("should only update process definition if instructions are empty")
    void shouldDoNothingIfInstructionsEmpty() {
        // Given
        var process = mock(ProcessExecution.class);
        when(process.id()).thenReturn(PROCESS_ID);
        var toDefinition = mock(ProcessDefinition.class);
        when(toDefinition.id()).thenReturn(TO_DEFINITION_ID);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of())
                .build();
        var command = SingleProcessMigrationCommand.of(process, toDefinition, migration);

        // When
        handler.handle(command);

        // Then
        verify(processPersistence).updateDefinitionId(TO_DEFINITION_ID, List.of(PROCESS_ID));
        verifyNoInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("should handle migration instructions for active activities")
    void shouldHandleInstructions() {
        // Given
        var process = mock(ProcessExecution.class);
        when(process.id()).thenReturn(PROCESS_ID);

        var activity = mock(Activity.class);
        when(activity.id()).thenReturn(ACTIVITY_ID);
        when(activity.definitionId()).thenReturn(FROM_ACTIVITY_ID);
        when(activity.isInTerminalState()).thenReturn(false);

        var activityExecution = mock(ActivityExecution.class);

        when(process.getActivitiesByDefinitionId(FROM_ACTIVITY_ID)).thenReturn(List.of(activity));
        when(process.activities()).thenReturn(List.of(activity));
        when(activityPersistence.findAll(List.of(ACTIVITY_ID))).thenReturn(List.of(activityExecution));

        var instruction = new ActivityMigrationInstruction(FROM_ACTIVITY_ID, TO_ACTIVITY_ID);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of(instruction))
                .build();
        var toDefinition = mock(ProcessDefinition.class);
        when(toDefinition.id()).thenReturn(TO_DEFINITION_ID);
        var command = SingleProcessMigrationCommand.of(process, toDefinition, migration);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(RunActivityCommand.of(TO_ACTIVITY_ID, PROCESS_ID));
        verify(dispatcher).dispatch(DeleteActivityCommand.of(activityExecution));
        verify(processPersistence).updateDefinitionId(TO_DEFINITION_ID, List.of(PROCESS_ID));
    }

    @Test
    @DisplayName("should ignore completed activities from migration but still delete them")
    void shouldIgnoreCompletedActivities() {
        // Given
        var process = mock(ProcessExecution.class);
        when(process.id()).thenReturn(PROCESS_ID);

        var activity = mock(Activity.class);
        when(activity.id()).thenReturn(ACTIVITY_ID);
        when(activity.definitionId()).thenReturn(FROM_ACTIVITY_ID);

        var activityExecution = mock(ActivityExecution.class);

        when(process.getActivitiesByDefinitionId(FROM_ACTIVITY_ID)).thenReturn(List.of());
        when(process.activities()).thenReturn(List.of(activity));
        when(activityPersistence.findAll(List.of(ACTIVITY_ID))).thenReturn(List.of(activityExecution));

        var instruction = new ActivityMigrationInstruction(FROM_ACTIVITY_ID, TO_ACTIVITY_ID);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of(instruction))
                .build();
        var toDefinition = mock(ProcessDefinition.class);
        when(toDefinition.id()).thenReturn(TO_DEFINITION_ID);
        var command = SingleProcessMigrationCommand.of(process, toDefinition, migration);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, never()).dispatch(any(RunActivityCommand.class));
        verify(dispatcher).dispatch(DeleteActivityCommand.of(activityExecution));
        verify(processPersistence).updateDefinitionId(TO_DEFINITION_ID, List.of(PROCESS_ID));
    }

    @Test
    @DisplayName("should delete activities that were not migrated")
    void shouldDeleteActivitiesNotMigrated() {
        // Given
        var migratedActId = "act-migrated";
        var otherActId = "act-other";

        var process = mock(ProcessExecution.class);
        when(process.id()).thenReturn(PROCESS_ID);

        var migratedActivity = mock(Activity.class);
        when(migratedActivity.id()).thenReturn(migratedActId);
        when(migratedActivity.definitionId()).thenReturn(FROM_ACTIVITY_ID);
        when(migratedActivity.isInTerminalState()).thenReturn(false);

        var otherActivity = mock(Activity.class);
        when(otherActivity.id()).thenReturn(otherActId);
        when(otherActivity.definitionId()).thenReturn(FROM_ACTIVITY_ID);

        var migratedActivityExecution = mock(ActivityExecution.class);
        var otherActivityExecution = mock(ActivityExecution.class);

        when(process.getActivitiesByDefinitionId(FROM_ACTIVITY_ID)).thenReturn(List.of(migratedActivity));
        when(process.activities()).thenReturn(List.of(migratedActivity, otherActivity));

        var instruction = new ActivityMigrationInstruction(FROM_ACTIVITY_ID, TO_ACTIVITY_ID);
        var migration = ProcessMigrationPlan.builder()
                .instructions(List.of(instruction))
                .build();
        var toDefinition = mock(ProcessDefinition.class);
        when(toDefinition.id()).thenReturn(TO_DEFINITION_ID);
        var command = SingleProcessMigrationCommand.of(process, toDefinition, migration);

        when(activityPersistence.findAll(List.of(otherActId, migratedActId))).thenReturn(List.of(otherActivityExecution, migratedActivityExecution));

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(RunActivityCommand.of(TO_ACTIVITY_ID, PROCESS_ID));
        verify(dispatcher).dispatch(DeleteActivityCommand.of(otherActivityExecution));
        verify(dispatcher).dispatch(DeleteActivityCommand.of(migratedActivityExecution));
        verify(processPersistence).updateDefinitionId(TO_DEFINITION_ID, List.of(PROCESS_ID));
    }

}
