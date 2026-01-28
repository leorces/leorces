package com.leorces.rest.client;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.client.client.DefinitionClient;
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
@DisplayName("Definition Service Implementation Tests")
class DefinitionServiceImplTest {

    private static final String DEFINITION_ID = "test-definition-123";
    private static final long OFFSET = 0L;
    private static final int LIMIT = 20;

    @Mock
    private DefinitionClient definitionClient;

    @InjectMocks
    private DefinitionServiceImpl definitionService;

    @Test
    @DisplayName("Should save definitions and return result")
    void shouldSaveDefinitionsAndReturnResult() {
        //Given
        var definitionsToSave = List.of(
                createProcessDefinition("def-1"),
                createProcessDefinition("def-2")
        );
        var savedDefinitions = List.of(
                createProcessDefinition("def-1"),
                createProcessDefinition("def-2")
        );
        when(definitionClient.save(definitionsToSave)).thenReturn(savedDefinitions);

        //When
        var result = definitionService.save(definitionsToSave);

        //Then
        verify(definitionClient).save(definitionsToSave);
        assertThat(result).isEqualTo(savedDefinitions);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should save empty list and return empty result")
    void shouldSaveEmptyListAndReturnEmptyResult() {
        //Given
        var emptyList = List.<ProcessDefinition>of();
        when(definitionClient.save(emptyList)).thenReturn(emptyList);

        //When
        var result = definitionService.save(emptyList);

        //Then
        verify(definitionClient).save(emptyList);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find definition by ID and return result when exists")
    void shouldFindDefinitionByIdAndReturnResultWhenExists() {
        //Given
        var expectedDefinition = createProcessDefinition(DEFINITION_ID);
        when(definitionClient.findById(DEFINITION_ID)).thenReturn(Optional.of(expectedDefinition));

        //When
        var result = definitionService.findById(DEFINITION_ID);

        //Then
        verify(definitionClient).findById(DEFINITION_ID);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDefinition);
    }

    @Test
    @DisplayName("Should return empty optional when definition not found by ID")
    void shouldReturnEmptyOptionalWhenDefinitionNotFoundById() {
        //Given
        when(definitionClient.findById(DEFINITION_ID)).thenReturn(Optional.empty());

        //When
        var result = definitionService.findById(DEFINITION_ID);

        //Then
        verify(definitionClient).findById(DEFINITION_ID);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all definitions with pagination and return result")
    void shouldFindAllDefinitionsWithPaginationAndReturnResult() {
        //Given
        var pageable = createPageable();
        var expectedDefinitions = List.of(
                createProcessDefinition("def-1"),
                createProcessDefinition("def-2")
        );
        var expectedPageableData = new PageableData<>(expectedDefinitions, 2L);
        when(definitionClient.findAll(pageable)).thenReturn(expectedPageableData);

        //When
        var result = definitionService.findAll(pageable);

        //Then
        verify(definitionClient).findAll(pageable);
        assertThat(result).isEqualTo(expectedPageableData);
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return empty result when no definitions found")
    void shouldReturnEmptyResultWhenNoDefinitionsFound() {
        //Given
        var pageable = createPageable();
        var emptyPageableData = new PageableData<ProcessDefinition>(List.of(), 0L);
        when(definitionClient.findAll(pageable)).thenReturn(emptyPageableData);

        //When
        var result = definitionService.findAll(pageable);

        //Then
        verify(definitionClient).findAll(pageable);
        assertThat(result).isEqualTo(emptyPageableData);
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(0L);
    }

    private Pageable createPageable() {
        return Pageable.builder()
                .offset(DefinitionServiceImplTest.OFFSET)
                .limit(DefinitionServiceImplTest.LIMIT)
                .build();
    }

    private ProcessDefinition createProcessDefinition(String id) {
        return ProcessDefinition.builder()
                .id(id)
                .build();
    }

}