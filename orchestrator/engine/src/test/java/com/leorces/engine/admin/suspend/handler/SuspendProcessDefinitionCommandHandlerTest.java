package com.leorces.engine.admin.suspend.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionByIdCommand;
import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionByKeyCommand;
import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionCommand;
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
@DisplayName("SuspendProcessDefinitionCommandHandler Tests")
class SuspendProcessDefinitionCommandHandlerTest {

    private static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    private static final String PROCESS_DEFINITION_VERSION = "processDefinitionVersion";
    private static final String KEY_VALUE = "testKey";
    private static final int VERSION_VALUE = 1;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private SuspendProcessDefinitionCommandHandler handler;

    @Test
    @DisplayName("should throw exception when input is invalid")
    void shouldThrowExceptionWhenInputIsInvalid() {
        // Given
        var command = new SuspendProcessDefinitionCommand(Map.of());

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isExactlyInstanceOf(ExecutionException.class)
                .hasMessage("Invalid input");
    }

    @Test
    @DisplayName("should dispatch SuspendProcessDefinitionByIdCommand when version is present")
    void shouldDispatchByIdCommand() {
        // Given
        var input = Map.<String, Object>of(
                PROCESS_DEFINITION_KEY, KEY_VALUE,
                PROCESS_DEFINITION_VERSION, VERSION_VALUE
        );
        var command = new SuspendProcessDefinitionCommand(input);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatchAsync(any(SuspendProcessDefinitionByIdCommand.class));
    }

    @Test
    @DisplayName("should dispatch SuspendProcessDefinitionByKeyCommand when version is absent")
    void shouldDispatchByKeyCommand() {
        // Given
        var input = Map.<String, Object>of(PROCESS_DEFINITION_KEY, KEY_VALUE);
        var command = new SuspendProcessDefinitionCommand(input);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(SuspendProcessDefinitionByKeyCommand.class));
    }

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType())
                .isEqualTo(SuspendProcessDefinitionCommand.class);
    }

}
