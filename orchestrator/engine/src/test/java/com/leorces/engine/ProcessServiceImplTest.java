package com.leorces.engine;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessServiceImpl Tests")
class ProcessServiceImplTest {

    @Mock
    private ProcessPersistence processPersistence;

    @InjectMocks
    private ProcessServiceImpl service;

    @Test
    @DisplayName("findAll should delegate to processPersistence")
    void findAllDelegates() {
        // Given
        var pageable = new Pageable(0, 10);
        PageableData<Process> data = new PageableData<>(List.of(), 0);
        when(processPersistence.findAll(pageable)).thenReturn(data);

        // When
        var result = service.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(data);
        verify(processPersistence).findAll(pageable);
        verifyNoMoreInteractions(processPersistence);
    }

    @Test
    @DisplayName("findById should delegate to processPersistence and return result")
    void findByIdDelegates() {
        // Given
        var processId = "proc1";
        var execution = mock(ProcessExecution.class);
        when(processPersistence.findExecutionById(processId)).thenReturn(Optional.of(execution));

        // When
        var result = service.findById(processId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(execution);
        verify(processPersistence).findExecutionById(processId);
        verifyNoMoreInteractions(processPersistence);
    }

    @Test
    @DisplayName("findById returns empty if process not found")
    void findByIdReturnsEmpty() {
        // Given
        var processId = "proc2";
        when(processPersistence.findExecutionById(processId)).thenReturn(Optional.empty());

        // When
        var result = service.findById(processId);

        // Then
        assertThat(result).isEmpty();
        verify(processPersistence).findExecutionById(processId);
        verifyNoMoreInteractions(processPersistence);
    }

}
