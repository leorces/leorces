package com.leorces.persistence.postgres;

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
@DisplayName("AdminPersistenceImpl Tests")
class AdminPersistenceImplTest {

    @Mock
    private ProcessPersistenceImpl processPersistence;

    @Mock
    private HistoryPersistence historyPersistence;

    @InjectMocks
    private AdminPersistenceImpl adminPersistence;

    @Test
    @DisplayName("doCompaction should save completed processes to history and return their count")
    void doCompactionShouldSaveCompletedProcessesToHistoryAndReturnCount() {
        // Given
        var batchSize = 10;
        var process1 = mock(ProcessExecution.class);
        var process2 = mock(ProcessExecution.class);
        var completedProcesses = List.of(process1, process2);

        when(processPersistence.findAllFullyCompletedForUpdate(batchSize)).thenReturn(completedProcesses);

        // When
        var result = adminPersistence.doCompaction(batchSize);

        // Then
        assertThat(result).isEqualTo(2);
        verify(historyPersistence).save(completedProcesses);
    }

    @Test
    @DisplayName("doCompaction should return zero when no completed processes found")
    void doCompactionShouldReturnZeroWhenNoProcessesFound() {
        // Given
        var batchSize = 10;
        when(processPersistence.findAllFullyCompletedForUpdate(batchSize)).thenReturn(List.of());

        // When
        var result = adminPersistence.doCompaction(batchSize);

        // Then
        assertThat(result).isZero();
        verifyNoInteractions(historyPersistence);
    }

}
