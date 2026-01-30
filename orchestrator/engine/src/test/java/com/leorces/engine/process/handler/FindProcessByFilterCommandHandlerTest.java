package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.process.command.FindProcessByFilterCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindProcessByFilterCommandHandler Tests")
class FindProcessByFilterCommandHandlerTest {

    @Mock
    private ProcessPersistence processPersistence;

    @InjectMocks
    private FindProcessByFilterCommandHandler handler;

    @Test
    @DisplayName("execute should return process when exactly one is found")
    void executeShouldReturnProcessWhenOneFound() {
        // Given
        var filter = ProcessFilter.builder().build();
        var command = new FindProcessByFilterCommand(filter);
        var process = Process.builder().id("p1").build();
        when(processPersistence.findAll(filter)).thenReturn(List.of(process));

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(process);
    }

    @Test
    @DisplayName("execute should return null when no process is found")
    void executeShouldReturnNullWhenNoneFound() {
        // Given
        var filter = ProcessFilter.builder().build();
        var command = new FindProcessByFilterCommand(filter);
        when(processPersistence.findAll(filter)).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("execute should throw exception when more than one process is found")
    void executeShouldThrowExceptionWhenMultipleFound() {
        // Given
        var filter = ProcessFilter.builder().build();
        var command = new FindProcessByFilterCommand(filter);
        var p1 = Process.builder().id("p1").build();
        var p2 = Process.builder().id("p2").build();
        when(processPersistence.findAll(filter)).thenReturn(List.of(p1, p2));

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("More than one process found");
    }

}