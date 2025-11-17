package com.leorces.rest.controller;

import com.leorces.api.HistoryService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryController Tests")
class HistoryControllerTest {

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 10;
    private static final String TEST_EXECUTION_ID = "test-execution-id";
    private static final String TEST_BUSINESS_KEY = "test-business-key";

    @Mock
    private HistoryService historyService;

    private HistoryController subject;

    @BeforeEach
    void setUp() {
        subject = new HistoryController(historyService);
    }

    @Test
    @DisplayName("Should find all process execution history with default parameters")
    void shouldFindAllProcessExecutionHistoryWithDefaultParameters() {
        // Given
        var expectedPageableData = createTestPageableData();
        when(historyService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(DEFAULT_OFFSET, DEFAULT_LIMIT).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(historyService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(DEFAULT_OFFSET);
        assertThat(capturedPageable.limit()).isEqualTo(DEFAULT_LIMIT);
    }

    @Test
    @DisplayName("Should find all process execution history with custom parameters")
    void shouldFindAllProcessExecutionHistoryWithCustomParameters() {
        // Given
        var customOffset = 20;
        var customLimit = 50;
        var expectedPageableData = createTestPageableData();

        when(historyService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(customOffset, customLimit).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(historyService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(customOffset);
        assertThat(capturedPageable.limit()).isEqualTo(customLimit);
    }

    @Test
    @DisplayName("Should handle zero offset and limit")
    void shouldHandleZeroOffsetAndLimit() {
        // Given
        var zeroOffset = 0;
        var zeroLimit = 0;
        var expectedPageableData = createTestPageableData();

        when(historyService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(zeroOffset, zeroLimit).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(historyService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(zeroOffset);
        assertThat(capturedPageable.limit()).isEqualTo(zeroLimit);
    }

    @Test
    @DisplayName("Should handle large offset and limit values")
    void shouldHandleLargeOffsetAndLimitValues() {
        // Given
        var largeOffset = 10000;
        var largeLimit = 1000;
        var expectedPageableData = createTestPageableData();

        when(historyService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(largeOffset, largeLimit).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(historyService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(largeOffset);
        assertThat(capturedPageable.limit()).isEqualTo(largeLimit);
    }

    @Test
    @DisplayName("Should handle negative offset and limit gracefully")
    void shouldHandleNegativeOffsetAndLimitGracefully() {
        // Given
        var negativeOffset = -5;
        var negativeLimit = -10;
        var expectedPageableData = createTestPageableData();

        when(historyService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(negativeOffset, negativeLimit).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(historyService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(negativeOffset);
        assertThat(capturedPageable.limit()).isEqualTo(negativeLimit);
    }

    @Test
    @DisplayName("Should return empty result when no history exists")
    void shouldReturnEmptyResultWhenNoHistoryExists() {
        // Given
        var emptyPageableData = new PageableData<ProcessExecution>(List.of(), 0L);
        when(historyService.findAll(any(Pageable.class))).thenReturn(emptyPageableData);

        // When
        var result = subject.findAll(DEFAULT_OFFSET, DEFAULT_LIMIT).getBody();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(emptyPageableData);
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(0L);
        verify(historyService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle single process execution in history")
    void shouldHandleSingleProcessExecutionInHistory() {
        // Given
        var singleExecution = createTestProcessExecution();
        var singleItemPageableData = new PageableData<>(List.of(singleExecution), 1L);
        when(historyService.findAll(any(Pageable.class))).thenReturn(singleItemPageableData);

        // When
        var result = subject.findAll(DEFAULT_OFFSET, DEFAULT_LIMIT).getBody();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(singleItemPageableData);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst()).isEqualTo(singleExecution);
        assertThat(result.total()).isEqualTo(1L);
        verify(historyService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle multiple process executions in history")
    void shouldHandleMultipleProcessExecutionsInHistory() {
        // Given
        var execution1 = createTestProcessExecution();
        var execution2 = ProcessExecution.builder()
                .id("execution-2")
                .businessKey("business-key-2")
                .build();
        var multipleItemsPageableData = new PageableData<>(List.of(execution1, execution2), 2L);
        when(historyService.findAll(any(Pageable.class))).thenReturn(multipleItemsPageableData);

        // When
        var result = subject.findAll(DEFAULT_OFFSET, DEFAULT_LIMIT).getBody();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(multipleItemsPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
        verify(historyService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should create Pageable with correct constructor parameters")
    void shouldCreatePageableWithCorrectConstructorParameters() {
        // Given
        var testOffset = 15;
        var testLimit = 25;
        var expectedPageableData = createTestPageableData();

        when(historyService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        subject.findAll(testOffset, testLimit);

        // Then
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(historyService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        // Verify that Pageable constructor was called with offset and limit only
        assertThat(capturedPageable.offset()).isEqualTo(testOffset);
        assertThat(capturedPageable.limit()).isEqualTo(testLimit);
        assertThat(capturedPageable.filter()).isEmpty(); // Default empty string
        assertThat(capturedPageable.state()).isEmpty(); // Default empty string
        assertThat(capturedPageable.sortByField()).isNull(); // Default null
        assertThat(capturedPageable.order()).isNull(); // Default null
    }

    private ProcessExecution createTestProcessExecution() {
        return ProcessExecution.builder()
                .id(TEST_EXECUTION_ID)
                .businessKey(TEST_BUSINESS_KEY)
                .build();
    }

    private PageableData<ProcessExecution> createTestPageableData() {
        var testExecution = createTestProcessExecution();
        return new PageableData<>(List.of(testExecution), 1L);
    }

}