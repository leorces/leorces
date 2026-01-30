package com.leorces.engine.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionDispatcherTest {

    @Test
    @DisplayName("Should dispatch command without result")
    void shouldDispatchCommandWithoutResult() {
        // Given
        var command = new TestCommand();
        CommandHandler<TestCommand> handler = mock(CommandHandler.class);
        when(handler.getCommandType()).thenAnswer(invocation -> TestCommand.class);
        var dispatcher = new ExecutionDispatcher(mockProvider(List.of(handler)));

        // When
        dispatcher.handle(command);

        // Then
        verify(handler).handle(command);
    }

    @Test
    @DisplayName("Should execute command and return result")
    void shouldExecuteCommandAndReturnResult() {
        // Given
        var command = new TestResultCommand();
        var result = "Success";
        ResultCommandHandler<TestResultCommand, String> handler = mock(ResultCommandHandler.class);
        when(handler.getCommandType()).thenAnswer(invocation -> TestResultCommand.class);
        when(handler.execute(command)).thenReturn(result);
        var dispatcher = new ExecutionDispatcher(mockProvider(List.of(handler)));

        // When
        var actualResult = dispatcher.execute(command);

        // Then
        assertEquals(result, actualResult);
        verify(handler).execute(command);
    }

    @Test
    @DisplayName("Should return null when executing result command with regular handler")
    void shouldReturnNullWhenExecutingResultCommandWithRegularHandler() {
        // Given
        var command = new TestResultCommand();
        CommandHandler<TestResultCommand> handler = mock(CommandHandler.class);
        when(handler.getCommandType()).thenAnswer(invocation -> TestResultCommand.class);
        var dispatcher = new ExecutionDispatcher(mockProvider(List.of(handler)));

        // When
        var actualResult = dispatcher.execute(command);

        // Then
        assertNull(actualResult);
        verify(handler).handle(command);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<List<CommandHandler<?>>> mockProvider(List<CommandHandler<?>> handlers) {
        ObjectProvider<List<CommandHandler<?>>> provider = mock(ObjectProvider.class);
        doAnswer(invocation -> {
            ((Consumer<List<CommandHandler<?>>>) invocation.getArgument(0)).accept(handlers);
            return null;
        }).when(provider).ifAvailable(any(Consumer.class));
        return provider;
    }

    private static class TestCommand implements ExecutionCommand {
    }

    private static class TestResultCommand implements ExecutionResultCommand<String> {
    }

}
