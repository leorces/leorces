package com.leorces.engine;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessService Implementation Tests")
class ProcessServiceImplTest {

    private static final int LIMIT = 10;
    private static final long OFFSET = 0L;
    private static final String PROCESS_ID_1 = "process-1";
    private static final String PROCESS_ID_2 = "process-2";
    private static final String NONEXISTENT_PROCESS_ID = "nonexistent-process";

    @Mock
    private ProcessPersistence processPersistence;

    private ProcessServiceImpl processService;

    @BeforeEach
    void setUp() {
        processService = new ProcessServiceImpl(processPersistence);
    }

    @Test
    @DisplayName("Should find all processes with pagination")
    void shouldFindAllProcessesWithPagination() {
        // Given
        var pageable = createPageable(OFFSET, LIMIT);
        var process1 = createProcess(PROCESS_ID_1);
        var process2 = createProcess(PROCESS_ID_2);
        var processes = List.of(process1, process2);
        var expectedPageableData = new PageableData<>(processes, 2L);

        when(processPersistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = processService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
        verify(processPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all processes returning empty page")
    void shouldFindAllProcessesReturningEmptyPage() {
        // Given
        var pageable = createPageable(OFFSET, LIMIT);
        var emptyProcesses = List.<Process>of();
        var expectedEmptyPageableData = new PageableData<>(emptyProcesses, 0L);

        when(processPersistence.findAll(pageable)).thenReturn(expectedEmptyPageableData);

        // When
        var result = processService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedEmptyPageableData);
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isZero();
        verify(processPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all processes with custom pagination parameters")
    void shouldFindAllProcessesWithCustomPaginationParameters() {
        // Given
        var customOffset = 20L;
        var customLimit = 5;
        var pageable = createPageable(customOffset, customLimit);
        var processes = List.of(createProcess(PROCESS_ID_1));
        var expectedPageableData = new PageableData<>(processes, 25L);

        when(processPersistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = processService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(25L);
        verify(processPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find process execution by ID when exists")
    void shouldFindProcessExecutionByIdWhenExists() {
        // Given
        var expectedProcessExecution = createProcessExecution();

        when(processPersistence.findExecutionById(PROCESS_ID_1)).thenReturn(Optional.of(expectedProcessExecution));

        // When
        var result = processService.findById(PROCESS_ID_1);

        // Then
        assertThat(result).isPresent().contains(expectedProcessExecution);
        verify(processPersistence).findExecutionById(PROCESS_ID_1);
    }

    @Test
    @DisplayName("Should return empty optional when process execution not found by ID")
    void shouldReturnEmptyOptionalWhenProcessExecutionNotFoundById() {
        // Given
        when(processPersistence.findExecutionById(NONEXISTENT_PROCESS_ID)).thenReturn(Optional.empty());

        // When
        var result = processService.findById(NONEXISTENT_PROCESS_ID);

        // Then
        assertThat(result).isEmpty();
        verify(processPersistence).findExecutionById(NONEXISTENT_PROCESS_ID);
    }

    @Test
    @DisplayName("Should handle large result sets for findAll")
    void shouldHandleLargeResultSetsForFindAll() {
        // Given
        var pageable = createPageable(OFFSET, 100);
        var largeProcesses = createMultipleProcesses();
        var expectedPageableData = new PageableData<>(largeProcesses, 1000L);

        when(processPersistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = processService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(50);
        assertThat(result.total()).isEqualTo(1000L);
        verify(processPersistence).findAll(pageable);
    }

    private Process createProcess(String processId) {
        var definition = createProcessDefinition();

        return Process.builder()
                .id(processId)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private ProcessExecution createProcessExecution() {
        var definition = createProcessDefinition();

        return ProcessExecution.builder()
                .id(ProcessServiceImplTest.PROCESS_ID_1)
                .definition(definition)
                .state(ProcessState.COMPLETED)
                .build();
    }

    private ProcessDefinition createProcessDefinition() {
        return ProcessDefinition.builder()
                .id("test-definition")
                .name("Test Process")
                .version(1)
                .build();
    }

    private List<Process> createMultipleProcesses() {
        return java.util.stream.IntStream.range(0, 50)
                .mapToObj(i -> createProcess("process-" + i))
                .toList();
    }

    private Pageable createPageable(long offset, int limit) {
        return Pageable.builder()
                .offset(offset)
                .limit(limit)
                .build();
    }

}