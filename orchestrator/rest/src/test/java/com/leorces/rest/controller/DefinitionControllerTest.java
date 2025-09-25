package com.leorces.rest.controller;

import com.leorces.api.DefinitionService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefinitionController Tests")
class DefinitionControllerTest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String DEFAULT_ORDER = "desc";
    private static final String DEFAULT_FILTER = "";
    private static final String TEST_DEFINITION_ID = "test-definition-id";
    private static final String TEST_DEFINITION_KEY = "test-definition-key";
    private static final String TEST_DEFINITION_NAME = "Test Process Definition";

    @Mock
    private DefinitionService definitionService;

    private DefinitionController subject;

    @BeforeEach
    void setUp() {
        subject = new DefinitionController(definitionService);
    }

    @Test
    @DisplayName("Should save single process definition successfully")
    void shouldSaveSingleProcessDefinitionSuccessfully() {
        // Given
        var definition = createTestProcessDefinition();
        var definitions = List.of(definition);
        var savedDefinitions = List.of(definition);

        when(definitionService.save(definitions)).thenReturn(savedDefinitions);

        // When
        var result = subject.save(definitions).getBody();

        // Then
        assertThat(result).isEqualTo(savedDefinitions);
        verify(definitionService).save(definitions);
    }

    @Test
    @DisplayName("Should save multiple process definitions successfully")
    void shouldSaveMultipleProcessDefinitionsSuccessfully() {
        // Given
        var definition1 = createTestProcessDefinition();
        var definition2 = ProcessDefinition.builder()
                .id("definition-2")
                .key("definition-key-2")
                .name("Definition 2")
                .build();
        var definitions = List.of(definition1, definition2);
        var savedDefinitions = List.of(definition1, definition2);

        when(definitionService.save(definitions)).thenReturn(savedDefinitions);

        // When
        var result = subject.save(definitions).getBody();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(savedDefinitions);
        verify(definitionService).save(definitions);
    }

    @Test
    @DisplayName("Should save empty list of definitions")
    void shouldSaveEmptyListOfDefinitions() {
        // Given
        var emptyDefinitions = List.<ProcessDefinition>of();
        var savedDefinitions = List.<ProcessDefinition>of();

        when(definitionService.save(emptyDefinitions)).thenReturn(savedDefinitions);

        // When
        var result = subject.save(emptyDefinitions).getBody();

        // Then
        assertThat(result).isEmpty();
        verify(definitionService).save(emptyDefinitions);
    }

    @Test
    @DisplayName("Should find all definitions with default parameters")
    void shouldFindAllDefinitionsWithDefaultParameters() {
        // Given
        var expectedPageableData = createTestPageableData();
        when(definitionService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_FIELD, DEFAULT_ORDER, DEFAULT_FILTER
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(definitionService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(0L);
        assertThat(capturedPageable.limit()).isEqualTo(DEFAULT_SIZE);
        assertThat(capturedPageable.sortByField()).isEqualTo(DEFAULT_SORT_FIELD);
        assertThat(capturedPageable.order()).isEqualTo(Pageable.Direction.DESC);
        assertThat(capturedPageable.filter()).isEqualTo(DEFAULT_FILTER);
    }

    @Test
    @DisplayName("Should find all definitions with custom parameters")
    void shouldFindAllDefinitionsWithCustomParameters() {
        // Given
        var customPage = 3;
        var customSize = 25;
        var customSortField = "name";
        var customOrder = "asc";
        var customFilter = "testFilter";
        var expectedPageableData = createTestPageableData();

        when(definitionService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                customPage, customSize, customSortField, customOrder, customFilter
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(definitionService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo((long) customPage * customSize);
        assertThat(capturedPageable.limit()).isEqualTo(customSize);
        assertThat(capturedPageable.sortByField()).isEqualTo(customSortField);
        assertThat(capturedPageable.order()).isEqualTo(Pageable.Direction.ASC);
        assertThat(capturedPageable.filter()).isEqualTo(customFilter);
    }

    @Test
    @DisplayName("Should find definition by ID when definition exists")
    void shouldFindDefinitionByIdWhenDefinitionExists() {
        // Given
        var expectedDefinition = createTestProcessDefinition();
        when(definitionService.findById(TEST_DEFINITION_ID)).thenReturn(Optional.of(expectedDefinition));

        // When
        ResponseEntity<ProcessDefinition> result = subject.findById(TEST_DEFINITION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedDefinition);
        verify(definitionService).findById(TEST_DEFINITION_ID);
    }

    @Test
    @DisplayName("Should return not found when definition does not exist")
    void shouldReturnNotFoundWhenDefinitionDoesNotExist() {
        // Given
        when(definitionService.findById(TEST_DEFINITION_ID)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProcessDefinition> result = subject.findById(TEST_DEFINITION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
        verify(definitionService).findById(TEST_DEFINITION_ID);
    }

    @Test
    @DisplayName("Should handle zero page and size parameters")
    void shouldHandleZeroPageAndSizeParameters() {
        // Given
        var zeroPage = 0;
        var zeroSize = 0;
        var expectedPageableData = createTestPageableData();

        when(definitionService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                zeroPage, zeroSize, DEFAULT_SORT_FIELD, DEFAULT_ORDER, DEFAULT_FILTER
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(definitionService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo(0L);
        assertThat(capturedPageable.limit()).isEqualTo(zeroSize);
    }

    @Test
    @DisplayName("Should handle large page numbers")
    void shouldHandleLargePageNumbers() {
        // Given
        var largePage = 500;
        var largeSize = 100;
        var expectedPageableData = createTestPageableData();

        when(definitionService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                largePage, largeSize, DEFAULT_SORT_FIELD, DEFAULT_ORDER, DEFAULT_FILTER
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(definitionService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.offset()).isEqualTo((long) largePage * largeSize);
        assertThat(capturedPageable.limit()).isEqualTo(largeSize);
    }

    @Test
    @DisplayName("Should handle empty filter parameter")
    void shouldHandleEmptyFilterParameter() {
        // Given
        var emptyFilter = "";
        var expectedPageableData = createTestPageableData();

        when(definitionService.findAll(any(Pageable.class))).thenReturn(expectedPageableData);

        // When
        var result = subject.findAll(
                DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_FIELD, DEFAULT_ORDER, emptyFilter
        ).getBody();

        // Then
        assertThat(result).isEqualTo(expectedPageableData);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(definitionService).findAll(pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.filter()).isEqualTo(emptyFilter);
    }

    @Test
    @DisplayName("Should find definition by ID with empty string ID")
    void shouldFindDefinitionByIdWithEmptyStringId() {
        // Given
        var emptyId = "";
        when(definitionService.findById(emptyId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProcessDefinition> result = subject.findById(emptyId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(definitionService).findById(emptyId);
    }

    private ProcessDefinition createTestProcessDefinition() {
        return ProcessDefinition.builder()
                .id(TEST_DEFINITION_ID)
                .key(TEST_DEFINITION_KEY)
                .name(TEST_DEFINITION_NAME)
                .build();
    }

    private PageableData<ProcessDefinition> createTestPageableData() {
        var testDefinition = createTestProcessDefinition();
        return new PageableData<>(List.of(testDefinition), 1L);
    }
}