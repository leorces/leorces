package com.leorces.rest.client.service;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.rest.client.client.HistoryClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("History Service Implementation Tests")
class HistoryServiceImplTest {

    private static final long OFFSET = 0L;
    private static final int LIMIT = 20;

    @Mock
    private HistoryClient historyClient;

    @InjectMocks
    private HistoryServiceImpl historyService;

    @Test
    @DisplayName("Should find all process executions with pagination and return result")
    void shouldFindAllProcessExecutionsWithPaginationAndReturnResult() {
        //Given
        var pageable = createPageable();
        var expectedProcessExecutions = List.of(
                createProcessExecution("execution-1"),
                createProcessExecution("execution-2")
        );
        var expectedPageableData = new PageableData<>(expectedProcessExecutions, 2L);
        when(historyClient.findAll(pageable)).thenReturn(expectedPageableData);

        //When
        var result = historyService.findAll(pageable);

        //Then
        verify(historyClient).findAll(pageable);
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return empty result when no process executions found")
    void shouldReturnEmptyResultWhenNoProcessExecutionsFound() {
        //Given
        var pageable = createPageable();
        var emptyPageableData = new PageableData<ProcessExecution>(List.of(), 0L);
        when(historyClient.findAll(pageable)).thenReturn(emptyPageableData);

        //When
        var result = historyService.findAll(pageable);

        //Then
        verify(historyClient).findAll(pageable);
        assertThat(result).isEqualTo(emptyPageableData);
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(0L);
    }

    private Pageable createPageable() {
        return Pageable.builder()
                .offset(HistoryServiceImplTest.OFFSET)
                .limit(HistoryServiceImplTest.LIMIT)
                .build();
    }

    private ProcessExecution createProcessExecution(String id) {
        return ProcessExecution.builder()
                .id(id)
                .build();
    }
}