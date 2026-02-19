package com.leorces.engine.admin.suspend.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionByIdCommand;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionByKeyCommand;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionCommand;
import com.leorces.engine.core.CommandDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeProcessDefinitionCommandHandler Tests")
class ResumeProcessDefinitionCommandHandlerTest {

    private static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    private static final String PROCESS_DEFINITION_VERSION = "processDefinitionVersion";
    private static final String KEY_VALUE = "testKey";
    private static final int VERSION_VALUE = 1;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private ResumeProcessDefinitionCommandHandler handler;

    @Test
    @DisplayName("should throw exception when input is invalid")
    void shouldThrowExceptionWhenInputIsInvalid() {
        // Given
        var command = new ResumeProcessDefinitionCommand(Map.of());

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isExactlyInstanceOf(ExecutionException.class)
                .hasMessage("Invalid input");
    }

    @Test
    @DisplayName("should dispatch ResumeProcessDefinitionByIdCommand when version is present")
    void shouldDispatchByIdCommand() {
        // Given
        var input = Map.<String, Object>of(
                PROCESS_DEFINITION_KEY, KEY_VALUE,
                PROCESS_DEFINITION_VERSION, VERSION_VALUE
        );
        var command = new ResumeProcessDefinitionCommand(input);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatchAsync(any(ResumeProcessDefinitionByIdCommand.class));
    }

    @Test
    @DisplayName("should dispatch ResumeProcessDefinitionByKeyCommand when version is absent")
    void shouldDispatchByKeyCommand() {
        // Given
        var input = Map.<String, Object>of(PROCESS_DEFINITION_KEY, KEY_VALUE);
        var command = new ResumeProcessDefinitionCommand(input);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(ResumeProcessDefinitionByKeyCommand.class));
    }

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType())
                .isEqualTo(ResumeProcessDefinitionCommand.class);
    }

}
