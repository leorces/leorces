package com.leorces.engine.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ExecutionDispatcher executionDispatcher;

    @InjectMocks
    private CommandDispatcher commandDispatcher;

    @Test
    @DisplayName("Should publish event on dispatch")
    void shouldPublishEventOnDispatch() {
        // Given
        var command = mock(ExecutionCommand.class);

        // When
        commandDispatcher.dispatch(command);

        // Then
        verify(publisher).publishEvent(command);
    }

    @Test
    @DisplayName("Should publish wrapped event on dispatchAsync")
    void shouldPublishWrappedEventOnDispatchAsync() {
        // Given
        var command = mock(ExecutionCommand.class);

        // When
        commandDispatcher.dispatchAsync(command);

        // Then
        verify(publisher).publishEvent(any(AsyncExecutionCommandWrapper.class));
    }

    @Test
    @DisplayName("Should call execution dispatcher on execute")
    void shouldCallExecutionDispatcherOnExecute() {
        // Given
        var command = mock(ExecutionResultCommand.class);
        var expectedResult = "result";
        when(executionDispatcher.execute(command)).thenReturn(expectedResult);

        // When
        var result = commandDispatcher.execute(command);

        // Then
        assertEquals(expectedResult, result);
        verify(executionDispatcher).execute(command);
    }

}
