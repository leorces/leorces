package com.leorces.engine.job.migration.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.job.migration.command.ValidateProcessMigrationCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateProcessMigrationCommandHandler Tests")
class ValidateProcessMigrationCommandHandlerTest {

    private static final String PROCESS_KEY = "testProcess";
    private static final String ACTIVITY_ID_1 = "activity1";
    private static final String ACTIVITY_ID_2 = "activity2";

    @InjectMocks
    private ValidateProcessMigrationCommandHandler handler;

    private ProcessDefinition fromDefinition;
    private ProcessDefinition toDefinition;
    private ProcessMigrationPlan migration;

    @BeforeEach
    void setUp() {
        fromDefinition = mock(ProcessDefinition.class);
        toDefinition = mock(ProcessDefinition.class);
        migration = ProcessMigrationPlan.builder()
                .definitionKey(PROCESS_KEY)
                .fromVersion(1)
                .toVersion(2)
                .instructions(List.of())
                .build();
    }

    @Test
    @DisplayName("Should successfully handle easy migration")
    void shouldHandleEasyMigration() {
        // Given
        var activity = mock(ActivityDefinition.class);
        when(activity.id()).thenReturn(ACTIVITY_ID_1);
        when(fromDefinition.activities()).thenReturn(List.of(activity));
        when(toDefinition.activities()).thenReturn(List.of(activity));

        var command = ValidateProcessMigrationCommand.of(fromDefinition, toDefinition, migration);

        // When & Then
        handler.handle(command);
    }

    @Test
    @DisplayName("Should throw exception when from-activity is invalid")
    void shouldThrowExceptionWhenFromActivityIsInvalid() {
        // Given
        var instruction = new ActivityMigrationInstruction(ACTIVITY_ID_1, ACTIVITY_ID_2);
        var migrationWithInstruction = migration.toBuilder()
                .instructions(List.of(instruction))
                .build();

        when(fromDefinition.activities()).thenReturn(List.of());

        var command = ValidateProcessMigrationCommand.of(fromDefinition, toDefinition, migrationWithInstruction);

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Can't migrate testProcess process from 1 to 2 version");
    }

    @Test
    @DisplayName("Should throw exception when to-activity is invalid")
    void shouldThrowExceptionWhenToActivityIsInvalid() {
        // Given
        var fromActivity = mock(ActivityDefinition.class);
        when(fromActivity.id()).thenReturn(ACTIVITY_ID_1);
        when(fromDefinition.activities()).thenReturn(List.of(fromActivity));

        var instruction = new ActivityMigrationInstruction(ACTIVITY_ID_1, ACTIVITY_ID_2);
        var migrationWithInstruction = migration.toBuilder()
                .instructions(List.of(instruction))
                .build();

        when(toDefinition.activities()).thenReturn(List.of());

        var command = ValidateProcessMigrationCommand.of(fromDefinition, toDefinition, migrationWithInstruction);

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Can't migrate testProcess process from 1 to 2 version");
    }

    @Test
    @DisplayName("Should throw exception when instruction for deleted activity is missing")
    void shouldThrowExceptionWhenInstructionForDeletedActivityIsMissing() {
        // Given
        var fromActivity1 = mock(ActivityDefinition.class);
        when(fromActivity1.id()).thenReturn(ACTIVITY_ID_1);
        var fromActivity2 = mock(ActivityDefinition.class);
        when(fromActivity2.id()).thenReturn(ACTIVITY_ID_2);

        when(fromDefinition.activities()).thenReturn(List.of(fromActivity1, fromActivity2));

        var toActivity1 = mock(ActivityDefinition.class);
        when(toActivity1.id()).thenReturn(ACTIVITY_ID_1);
        when(toDefinition.activities()).thenReturn(List.of(toActivity1));

        when(fromDefinition.id()).thenReturn("fromId");
        when(toDefinition.id()).thenReturn("toId");

        var command = ValidateProcessMigrationCommand.of(fromDefinition, toDefinition, migration);

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Can't migrate process from definitionId: fromId to definitionId: toId. Missing instructions for activities: [activity2]");
    }

}
