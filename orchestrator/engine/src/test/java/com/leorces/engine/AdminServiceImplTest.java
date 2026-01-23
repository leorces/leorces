package com.leorces.engine;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceImpl Tests")
class AdminServiceImplTest {

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("doCompaction should dispatch CompactionCommand asynchronously")
    void doCompactionShouldDispatchCompactionCommandAsynchronously() {
        // When
        adminService.doCompaction();

        // Then
        verify(dispatcher).dispatchAsync(any(CompactionCommand.class));
    }

}
