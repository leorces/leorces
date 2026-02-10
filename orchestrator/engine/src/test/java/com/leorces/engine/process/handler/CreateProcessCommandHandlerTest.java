package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CreateProcessByCallActivityCommand;
import com.leorces.engine.process.command.CreateProcessCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProcessCommandHandler Tests")
class CreateProcessCommandHandlerTest {

    private static final String DEFINITION_ID = "definition-id";
    private static final String DEFINITION_KEY = "definition-key";
    private static final String BUSINESS_KEY = "business-key";

    @Mock
    private VariablesMapper variablesMapper;
    @Mock
    private DefinitionPersistence definitionPersistence;
    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private CreateProcessCommandHandler handler;

    @Test
    @DisplayName("Should delegate to CreateProcessByCallActivityCommand when callActivity is present")
    void executeWithCallActivity() {
        // Given
        var callActivity = ActivityExecution.builder().id("activity-id").build();
        var command = CreateProcessCommand.byCallActivity(callActivity);
        var expectedProcess = Process.builder().id("process-id").build();
        when(dispatcher.execute(any(CreateProcessByCallActivityCommand.class))).thenReturn(expectedProcess);

        // When
        var result = handler.execute(command);

        // Then
        assertEquals(expectedProcess, result);
        verify(dispatcher).execute(CreateProcessByCallActivityCommand.of(callActivity));
    }

    @Test
    @DisplayName("Should build process when definitionId is provided")
    void executeWithDefinitionId() {
        // Given
        var variables = Map.<String, Object>of("key", "value");
        var command = CreateProcessCommand.byDefinitionId(DEFINITION_ID, BUSINESS_KEY, variables);
        var definition = ProcessDefinition.builder().id(DEFINITION_ID).build();

        when(definitionPersistence.findById(DEFINITION_ID)).thenReturn(Optional.of(definition));
        when(variablesMapper.map(variables)).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertProcess(result, definition);
        verify(definitionPersistence).findById(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should build process when definitionKey is provided")
    void executeWithDefinitionKey() {
        // Given
        var variables = Map.<String, Object>of("key", "value");
        var command = CreateProcessCommand.byDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, variables);
        var definition = ProcessDefinition.builder().key(DEFINITION_KEY).build();

        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variablesMapper.map(variables)).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertProcess(result, definition);
        verify(definitionPersistence).findLatestByKey(DEFINITION_KEY);
    }

    @Test
    @DisplayName("Should throw exception when definition not found by id")
    void executeWithNonExistentDefinitionId() {
        // Given
        var command = CreateProcessCommand.byDefinitionId(DEFINITION_ID, BUSINESS_KEY, Map.of());
        when(definitionPersistence.findById(DEFINITION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExecutionException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw exception when definition not found by key")
    void executeWithNonExistentDefinitionKey() {
        // Given
        var command = CreateProcessCommand.byDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, Map.of());
        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExecutionException.class, () -> handler.execute(command));
    }

    private void assertProcess(Process result, ProcessDefinition definition) {
        assertNotNull(result);
        assertEquals(definition, result.definition());
        assertEquals(BUSINESS_KEY, result.businessKey());
    }

}
