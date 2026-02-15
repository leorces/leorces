package com.leorces.engine.definition.handler;

import com.leorces.engine.definition.command.SaveDefinitionsCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaveDefinitionsCommandHandler Tests")
class SaveDefinitionsCommandHandlerTest {

    private static final String MESSAGE_REFERENCE = "msg-1";
    private static final String UNUSED_MESSAGE = "unused-msg";

    @Mock
    private DefinitionPersistence definitionPersistence;

    @InjectMocks
    private SaveDefinitionsCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When
        var commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(SaveDefinitionsCommand.class);
    }

    @Test
    @DisplayName("should normalize and save definitions")
    void shouldNormalizeAndSaveDefinitions() {
        // Given
        var activity = mock(MessageActivityDefinition.class);
        when(activity.messageReference()).thenReturn(MESSAGE_REFERENCE);

        var definition = ProcessDefinition.builder()
                .activities(List.of(activity))
                .messages(List.of(MESSAGE_REFERENCE, UNUSED_MESSAGE))
                .build();
        var command = new SaveDefinitionsCommand(List.of(definition));

        when(definitionPersistence.save(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).hasSize(1);
        var normalizedDefinition = result.getFirst();
        assertThat(normalizedDefinition.messages()).containsExactly(MESSAGE_REFERENCE);
        assertThat(normalizedDefinition.messages()).doesNotContain(UNUSED_MESSAGE);
    }

    @Test
    @DisplayName("should handle empty definitions list")
    void shouldHandleEmptyDefinitionsList() {
        // Given
        var command = new SaveDefinitionsCommand(List.of());
        when(definitionPersistence.save(List.of())).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEmpty();
    }

}
