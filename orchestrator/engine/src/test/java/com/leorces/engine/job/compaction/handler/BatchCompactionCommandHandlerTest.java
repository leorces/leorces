package com.leorces.engine.job.compaction.handler;

import com.leorces.engine.job.compaction.command.BatchCompactionCommand;
import com.leorces.persistence.AdminPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchCompactionCommandHandler Tests")
class BatchCompactionCommandHandlerTest {

    @Mock
    private AdminPersistence adminPersistence;

    @InjectMocks
    private BatchCompactionCommandHandler handler;

    @Test
    @DisplayName("handle should perform compaction once if less than batch size is compacted")
    void handleShouldPerformCompactionOnce() {
        // Given
        var batchSize = 10;
        var command = BatchCompactionCommand.of(batchSize);
        when(adminPersistence.doCompaction(batchSize)).thenReturn(5);

        // When
        handler.handle(command);

        // Then
        verify(adminPersistence, times(1)).doCompaction(batchSize);
    }

    @Test
    @DisplayName("handle should perform compaction multiple times until results are less than batch size")
    void handleShouldPerformCompactionMultipleTimes() {
        // Given
        var batchSize = 10;
        var command = BatchCompactionCommand.of(batchSize);
        when(adminPersistence.doCompaction(batchSize))
                .thenReturn(10)
                .thenReturn(10)
                .thenReturn(3);

        // When
        handler.handle(command);

        // Then
        verify(adminPersistence, times(3)).doCompaction(batchSize);
    }

}
