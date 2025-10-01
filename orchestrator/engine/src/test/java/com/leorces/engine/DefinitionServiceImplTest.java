package com.leorces.engine;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.DefinitionPersistence;
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
@DisplayName("DefinitionServiceImpl Tests")
class DefinitionServiceImplTest {

    @Mock
    private DefinitionPersistence persistence;

    @InjectMocks
    private DefinitionServiceImpl service;

    @Test
    @DisplayName("save should delegate to persistence")
    void saveDelegates() {
        // Given
        var definition = mock(ProcessDefinition.class);
        var definitions = List.of(definition);
        when(persistence.save(definitions)).thenReturn(definitions);

        // When
        var result = service.save(definitions);

        // Then
        assertThat(result).isEqualTo(definitions);
        verify(persistence).save(definitions);
        verifyNoMoreInteractions(persistence);
    }

    @Test
    @DisplayName("findById should delegate to persistence")
    void findByIdDelegates() {
        // Given
        var definitionId = "def-1";
        var definition = mock(ProcessDefinition.class);
        when(persistence.findById(definitionId)).thenReturn(Optional.of(definition));

        // When
        var result = service.findById(definitionId);

        // Then
        assertThat(result).contains(definition);
        verify(persistence).findById(definitionId);
        verifyNoMoreInteractions(persistence);
    }

    @Test
    @DisplayName("findAll should delegate to persistence")
    void findAllDelegates() {
        // Given
        Pageable pageable = new Pageable(0, 10);
        PageableData<ProcessDefinition> data = new PageableData<>(List.of(), 0);
        when(persistence.findAll(pageable)).thenReturn(data);

        // When
        var result = service.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(data);
        verify(persistence).findAll(pageable);
        verifyNoMoreInteractions(persistence);
    }

}
