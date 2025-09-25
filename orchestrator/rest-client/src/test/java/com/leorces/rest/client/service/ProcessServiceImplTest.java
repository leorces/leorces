package com.leorces.rest.client.service;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.rest.client.client.ProcessClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Process Service Implementation Tests")
class ProcessServiceImplTest {

    private static final String PROCESS_ID = "test-process-123";
    private static final long OFFSET = 0L;
    private static final int LIMIT = 20;

    @Mock
    private ProcessClient processClient;

    @InjectMocks
    private ProcessServiceImpl processService;

    @Test
    @DisplayName("Should find all processes with pagination and return result")
    void shouldFindAllProcessesWithPaginationAndReturnResult() {
        //Given
        var pageable = createPageable();
        var expectedProcesses = List.of(
                createProcess("process-1"),
                createProcess("process-2")
        );
        var expectedPageableData = new PageableData<>(expectedProcesses, 2L);
        when(processClient.findAll(pageable)).thenReturn(expectedPageableData);

        //When
        var result = processService.findAll(pageable);

        //Then
        verify(processClient).findAll(pageable);
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should find process by ID and return result when exists")
    void shouldFindProcessByIdAndReturnResultWhenExists() {
        //Given
        var expectedProcessExecution = createProcessExecution(PROCESS_ID);
        when(processClient.findById(PROCESS_ID)).thenReturn(Optional.of(expectedProcessExecution));

        //When
        var result = processService.findById(PROCESS_ID);

        //Then
        verify(processClient).findById(PROCESS_ID);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedProcessExecution);
    }

    @Test
    @DisplayName("Should return empty optional when process not found by ID")
    void shouldReturnEmptyOptionalWhenProcessNotFoundById() {
        //Given
        when(processClient.findById(PROCESS_ID)).thenReturn(Optional.empty());

        //When
        var result = processService.findById(PROCESS_ID);

        //Then
        verify(processClient).findById(PROCESS_ID);
        assertThat(result).isEmpty();
    }

    private Pageable createPageable() {
        return Pageable.builder()
                .offset(ProcessServiceImplTest.OFFSET)
                .limit(ProcessServiceImplTest.LIMIT)
                .build();
    }

    private Process createProcess(String id) {
        return Process.builder()
                .id(id)
                .build();
    }

    private ProcessExecution createProcessExecution(String id) {
        return ProcessExecution.builder()
                .id(id)
                .build();
    }
}