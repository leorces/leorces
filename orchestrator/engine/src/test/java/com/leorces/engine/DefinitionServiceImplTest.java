package com.leorces.engine;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.DefinitionPersistence;
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
@DisplayName("DefinitionService Implementation Tests")
class DefinitionServiceImplTest {

    private static final String DEFINITION_ID = "test-definition-id";
    private static final int LIMIT = 10;
    private static final long OFFSET = 0L;

    @Mock
    private DefinitionPersistence persistence;

    private DefinitionServiceImpl definitionService;

    @BeforeEach
    void setUp() {
        definitionService = new DefinitionServiceImpl(persistence);
    }

    @Test
    @DisplayName("Should save process definitions successfully")
    void shouldSaveProcessDefinitionsSuccessfully() {
        // Given
        var definition1 = createProcessDefinition("def1");
        var definition2 = createProcessDefinition("def2");
        var definitions = List.of(definition1, definition2);
        var expectedSavedDefinitions = List.of(definition1, definition2);

        when(persistence.save(definitions)).thenReturn(expectedSavedDefinitions);

        // When
        var result = definitionService.save(definitions);

        // Then
        assertThat(result).isEqualTo(expectedSavedDefinitions);
        verify(persistence).save(definitions);
    }

    @Test
    @DisplayName("Should save empty list of process definitions")
    void shouldSaveEmptyListOfProcessDefinitions() {
        // Given
        var emptyDefinitions = List.<ProcessDefinition>of();

        when(persistence.save(emptyDefinitions)).thenReturn(emptyDefinitions);

        // When
        var result = definitionService.save(emptyDefinitions);

        // Then
        assertThat(result).isEmpty();
        verify(persistence).save(emptyDefinitions);
    }

    @Test
    @DisplayName("Should find process definition by ID when exists")
    void shouldFindProcessDefinitionByIdWhenExists() {
        // Given
        var expectedDefinition = createProcessDefinition(DEFINITION_ID);

        when(persistence.findById(DEFINITION_ID)).thenReturn(Optional.of(expectedDefinition));

        // When
        var result = definitionService.findById(DEFINITION_ID);

        // Then
        assertThat(result).isPresent().contains(expectedDefinition);
        verify(persistence).findById(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should return empty optional when process definition not found by ID")
    void shouldReturnEmptyOptionalWhenProcessDefinitionNotFoundById() {
        // Given
        when(persistence.findById(DEFINITION_ID)).thenReturn(Optional.empty());

        // When
        var result = definitionService.findById(DEFINITION_ID);

        // Then
        assertThat(result).isEmpty();
        verify(persistence).findById(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should find all process definitions with pagination")
    void shouldFindAllProcessDefinitionsWithPagination() {
        // Given
        var pageable = createPageable();
        var definition1 = createProcessDefinition("def1");
        var definition2 = createProcessDefinition("def2");
        var definitions = List.of(definition1, definition2);
        var expectedPageableData = new PageableData<>(definitions, 2L);

        when(persistence.findAll(pageable)).thenReturn(expectedPageableData);

        // When
        var result = definitionService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
        verify(persistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all process definitions returning empty page")
    void shouldFindAllProcessDefinitionsReturningEmptyPage() {
        // Given
        var pageable = createPageable();
        var emptyDefinitions = List.<ProcessDefinition>of();
        var expectedEmptyPageableData = new PageableData<>(emptyDefinitions, 0L);

        when(persistence.findAll(pageable)).thenReturn(expectedEmptyPageableData);

        // When
        var result = definitionService.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(expectedEmptyPageableData);
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isZero();
        verify(persistence).findAll(pageable);
    }

    private ProcessDefinition createProcessDefinition(String id) {
        return ProcessDefinition.builder()
                .id(id)
                .name("Test Process")
                .version(1)
                .build();
    }

    private Pageable createPageable() {
        return Pageable.builder()
                .offset(DefinitionServiceImplTest.OFFSET)
                .limit(DefinitionServiceImplTest.LIMIT)
                .build();
    }

}