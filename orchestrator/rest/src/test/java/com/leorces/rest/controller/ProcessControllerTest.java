package com.leorces.rest.controller;

import com.leorces.api.ProcessService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessController Tests")
class ProcessControllerTest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String DEFAULT_ORDER = "desc";
    private static final String DEFAULT_FILTER = "";
    private static final String DEFAULT_STATE = "";
    private static final String TEST_PROCESS_ID = "test-process-id";

    @Mock
    private ProcessService processService;

    private ProcessController subject;

    @BeforeEach
    void setUp() {
        subject = new ProcessController(processService);
    }

    @Test
    @DisplayName("Should find all processes with default parameters")
    void shouldFindAllProcessesWithDefaultParameters() {
        // Given
        var expectedPageableData = createTestPageableData();
        when(processService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_FIELD,
                DEFAULT_ORDER, DEFAULT_FILTER, DEFAULT_STATE
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(processService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(0L);
        assertThat(capturedPageable.limit()).isEqualTo(DEFAULT_SIZE);
        assertThat(capturedPageable.sortByField()).isEqualTo(DEFAULT_SORT_FIELD);
        assertThat(capturedPageable.order()).isEqualTo(Pageable.Direction.DESC);
        assertThat(capturedPageable.filter()).isEqualTo(DEFAULT_FILTER);
        assertThat(capturedPageable.state()).isEqualTo(DEFAULT_STATE);
    }

    @Test
    @DisplayName("Should find all processes with custom parameters")
    void shouldFindAllProcessesWithCustomParameters() {
        // Given
        var customPage = 2;
        var customSize = 20;
        var customSortField = "name";
        var customOrder = "asc";
        var customFilter = "testFilter";
        var customState = "active";
        var expectedPageableData = createTestPageableData();

        when(processService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                customPage, customSize, customSortField,
                customOrder, customFilter, customState
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(processService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo((long) customPage * customSize);
        assertThat(capturedPageable.limit()).isEqualTo(customSize);
        assertThat(capturedPageable.sortByField()).isEqualTo(customSortField);
        assertThat(capturedPageable.order()).isEqualTo(Pageable.Direction.ASC);
        assertThat(capturedPageable.filter()).isEqualTo(customFilter);
        assertThat(capturedPageable.state()).isEqualTo(customState);
    }

    @Test
    @DisplayName("Should find process by ID when process exists")
    void shouldFindProcessByIdWhenProcessExists() {
        // Given
        var expectedProcess = createTestProcessExecution();
        when(processService.findById(TEST_PROCESS_ID)).thenReturn(Optional.of(expectedProcess));

        // When
        var result = subject.findById(TEST_PROCESS_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedProcess);
        verify(processService).findById(TEST_PROCESS_ID);
    }

    @Test
    @DisplayName("Should return not found when process does not exist")
    void shouldReturnNotFoundWhenProcessDoesNotExist() {
        // Given
        when(processService.findById(TEST_PROCESS_ID)).thenReturn(Optional.empty());

        // When
        var result = subject.findById(TEST_PROCESS_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
        verify(processService).findById(TEST_PROCESS_ID);
    }

    @Test
    @DisplayName("Should handle zero page and size parameters")
    void shouldHandleZeroPageAndSizeParameters() {
        // Given
        var zeroPage = 0;
        var zeroSize = 0;
        var expectedPageableData = createTestPageableData();

        when(processService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                zeroPage, zeroSize, DEFAULT_SORT_FIELD,
                DEFAULT_ORDER, DEFAULT_FILTER, DEFAULT_STATE
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(processService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(0L);
        assertThat(capturedPageable.limit()).isEqualTo(zeroSize);
    }

    @Test
    @DisplayName("Should handle large page numbers")
    void shouldHandleLargePageNumbers() {
        // Given
        var largePage = 1000;
        var largeSize = 50;
        var expectedPageableData = createTestPageableData();

        when(processService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                largePage, largeSize, DEFAULT_SORT_FIELD,
                DEFAULT_ORDER, DEFAULT_FILTER, DEFAULT_STATE
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(processService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo((long) largePage * largeSize);
        assertThat(capturedPageable.limit()).isEqualTo(largeSize);
    }

    private PageableData<Process> createTestPageableData() {
        var testProcess = Process.builder()
                .id(TEST_PROCESS_ID)
                .build();

        return new PageableData<>(List.of(testProcess), 1L);
    }

    private ProcessExecution createTestProcessExecution() {
        return ProcessExecution.builder()
                .id(TEST_PROCESS_ID)
                .build();
    }

}