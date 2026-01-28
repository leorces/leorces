package com.leorces.engine.admin.migration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.engine.admin.migration.command.EasyProcessMigrationCommand;
import com.leorces.engine.admin.migration.command.ProcessMigrationCommand;
import com.leorces.engine.admin.migration.command.ProcessMigrationWithInstructionsCommand;
import com.leorces.engine.admin.migration.command.ValidateProcessMigrationCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessMigrationCommandHandler Tests")
class ProcessMigrationCommandHandlerTest {

    @Mock
    private DefinitionPersistence definitionPersistence;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private ProcessMigrationCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(ProcessMigrationCommand.class);
    }

    @Test
    @DisplayName("should handle easy migration")
    void shouldHandleEasyMigration() {
        // Given
        var key = "proc-key";
        var input = Map.of(
                "definitionKey", key,
                "fromVersion", 1,
                "toVersion", 2,
                "instructions", List.of()
        );
        var command = new ProcessMigrationCommand(input);

        var fromDef = mock(ProcessDefinition.class);
        var toDef = mock(ProcessDefinition.class);
        when(fromDef.activities()).thenReturn(List.of());
        when(toDef.activities()).thenReturn(List.of());

        when(definitionPersistence.findByKeyAndVersion(key, 1)).thenReturn(Optional.of(fromDef));
        when(definitionPersistence.findByKeyAndVersion(key, 2)).thenReturn(Optional.of(toDef));

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(ValidateProcessMigrationCommand.class));
        verify(dispatcher).dispatchAsync(any(EasyProcessMigrationCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(ProcessMigrationWithInstructionsCommand.class));
    }

    @Test
    @DisplayName("should handle migration with instructions")
    void shouldHandleMigrationWithInstructions() {
        // Given
        var key = "proc-key";
        var input = Map.of(
                "definitionKey", key,
                "fromVersion", 1,
                "toVersion", 2,
                "instructions", List.of(Map.of("fromActivityId", "a", "toActivityId", "b"))
        );
        var command = new ProcessMigrationCommand(input);

        var fromDef = mock(ProcessDefinition.class);
        var toDef = mock(ProcessDefinition.class);

        when(definitionPersistence.findByKeyAndVersion(key, 1)).thenReturn(Optional.of(fromDef));
        when(definitionPersistence.findByKeyAndVersion(key, 2)).thenReturn(Optional.of(toDef));

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(ValidateProcessMigrationCommand.class));
        verify(dispatcher).dispatchAsync(any(ProcessMigrationWithInstructionsCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(EasyProcessMigrationCommand.class));
    }

}
