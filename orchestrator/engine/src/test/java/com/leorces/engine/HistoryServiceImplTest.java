package com.leorces.engine;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.HistoryPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryServiceImpl Tests")
class HistoryServiceImplTest {

    @Mock
    private HistoryPersistence historyPersistence;

    @InjectMocks
    private HistoryServiceImpl service;

    @Test
    @DisplayName("findAll should delegate to historyPersistence")
    void findAllDelegates() {
        // Given
        var pageable = new Pageable(0, 10);
        PageableData<ProcessExecution> data = new PageableData<>(List.of(), 0);
        when(historyPersistence.findAll(pageable)).thenReturn(data);

        // When
        var result = service.findAll(pageable);

        // Then
        assertThat(result).isEqualTo(data);
        verify(historyPersistence).findAll(pageable);
        verifyNoMoreInteractions(historyPersistence);
    }

}
