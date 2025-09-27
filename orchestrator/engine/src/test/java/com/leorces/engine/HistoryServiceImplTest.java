package com.leorces.engine;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.HistoryPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryService Implementation Tests")
class HistoryServiceImplTest {

    private static final int LIMIT = 10;
    private static final long OFFSET = 0L;
    private static final String PROCESS_ID_1 = "process-1";
    private static final String PROCESS_ID_2 = "process-2";

    @Mock
    private HistoryPersistence historyPersistence;

    private HistoryServiceImpl historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryServiceImpl(historyPersistence);
    }

    @Test
    @DisplayName("Should find all process executions with pagination")
    void shouldFindAllProcessExecutionsWithPagination() {
        // Given
        var pageable = createPageable(OFFSET, LIMIT);
        var processExecution1 = createProcessExecution(PROCESS_ID_1);
        var processExecution2 = createProcessExecution(PROCESS_ID_2);
        var processExecutions = List.of(processExecution1, processExecution2);
        var expectedPageableData = new PageableData<>(processExecutions, 2L);

        when(historyPersistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = historyService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
        verify(historyPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all process executions returning empty page")
    void shouldFindAllProcessExecutionsReturningEmptyPage() {
        // Given
        var pageable = createPageable(OFFSET, LIMIT);
        var emptyProcessExecutions = List.<ProcessExecution>of();
        var expectedEmptyPageableData = new PageableData<>(emptyProcessExecutions, 0L);

        when(historyPersistence.findAll(pageable)).thenReturn(expectedEmptyPageableData);

        // When
        var result = historyService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedEmptyPageableData);
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isZero();
        verify(historyPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all process executions with custom pagination parameters")
    void shouldFindAllProcessExecutionsWithCustomPaginationParameters() {
        // Given
        var customOffset = 20L;
        var customLimit = 5;
        var pageable = createPageable(customOffset, customLimit);
        var processExecutions = List.of(createProcessExecution(PROCESS_ID_1));
        var expectedPageableData = new PageableData<>(processExecutions, 25L);

        when(historyPersistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = historyService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(25L);
        verify(historyPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should handle large result sets")
    void shouldHandleLargeResultSets() {
        // Given
        var pageable = createPageable(OFFSET, 100);
        var largeProcessExecutions = createMultipleProcessExecutions();
        var expectedPageableData = new PageableData<>(largeProcessExecutions, 1000L);

        when(historyPersistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = historyService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(50);
        assertThat(result.total()).isEqualTo(1000L);
        verify(historyPersistence).findAll(pageable);
    }

    private ProcessExecution createProcessExecution(String processId) {
        var definition = ProcessDefinition.builder()
                .id("test-definition")
                .name("Test Process")
                .version(1)
                .build();

        return ProcessExecution.builder()
                .id(processId)
                .definition(definition)
                .state(ProcessState.COMPLETED)
                .build();
    }

    private List<ProcessExecution> createMultipleProcessExecutions() {
        return java.util.stream.IntStream.range(0, 50)
                .mapToObj(i -> createProcessExecution("process-" + i))
                .toList();
    }

    private Pageable createPageable(long offset, int limit) {
        return Pageable.builder()
                .offset(offset)
                .limit(limit)
                .build();
    }

}