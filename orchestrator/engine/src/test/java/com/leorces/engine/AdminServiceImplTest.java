package com.leorces.engine;

import com.leorces.engine.configuration.properties.CompactionProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.HistoryPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Implementation Tests")
class AdminServiceImplTest {

    private static final int BATCH_SIZE = 10;

    @Mock
    private HistoryPersistence historyPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private CompactionProperties compactionProperties;

    @Mock
    private TaskExecutorService taskExecutor;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(historyPersistence, processPersistence, compactionProperties, taskExecutor);
    }

    @Test
    @DisplayName("Should execute compaction task asynchronously")
    void shouldExecuteCompactionTaskAsynchronously() {
        // Given & When
        adminService.doCompaction();

        // Then
        verify(taskExecutor).execute(any(Runnable.class));
        verifyNoInteractions(processPersistence);
        verifyNoInteractions(historyPersistence);
    }

    @Test
    @DisplayName("Should process single batch of completed processes successfully")
    void shouldProcessSingleBatchOfCompletedProcessesSuccessfully() {
        // Given
        when(compactionProperties.batchSize()).thenReturn(BATCH_SIZE);
        var completedProcesses = createCompletedProcessExecutions(5);

        when(processPersistence.findAllFullyCompleted(BATCH_SIZE)).thenReturn(completedProcesses);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // When
        adminService.doCompaction();
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run(); // Execute the captured task

        // Then
        verify(processPersistence).findAllFullyCompleted(BATCH_SIZE);
        verify(historyPersistence).save(completedProcesses);
    }

    @Test
    @DisplayName("Should process multiple batches of completed processes")
    void shouldProcessMultipleBatchesOfCompletedProcesses() {
        // Given
        when(compactionProperties.batchSize()).thenReturn(BATCH_SIZE);
        var firstBatch = createCompletedProcessExecutions(BATCH_SIZE);
        var secondBatch = createCompletedProcessExecutions(5);
        var emptyBatch = List.<ProcessExecution>of();

        when(processPersistence.findAllFullyCompleted(BATCH_SIZE))
                .thenReturn(firstBatch)
                .thenReturn(secondBatch)
                .thenReturn(emptyBatch);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // When
        adminService.doCompaction();
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run(); // Execute the captured task

        // Then
        verify(processPersistence, times(2)).findAllFullyCompleted(BATCH_SIZE);
        verify(historyPersistence).save(firstBatch);
        verify(historyPersistence).save(secondBatch);
        verifyNoMoreInteractions(historyPersistence);
    }

    @Test
    @DisplayName("Should stop compaction when no completed processes found")
    void shouldStopCompactionWhenNoCompletedProcessesFound() {
        // Given
        when(compactionProperties.batchSize()).thenReturn(BATCH_SIZE);
        var emptyProcesses = List.<ProcessExecution>of();

        when(processPersistence.findAllFullyCompleted(BATCH_SIZE)).thenReturn(emptyProcesses);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // When
        adminService.doCompaction();
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run(); // Execute the captured task

        // Then
        verify(processPersistence).findAllFullyCompleted(BATCH_SIZE);
        verifyNoInteractions(historyPersistence);
    }

    @Test
    @DisplayName("Should handle different batch sizes from configuration")
    void shouldHandleDifferentBatchSizesFromConfiguration() {
        // Given
        var customBatchSize = 25;
        when(compactionProperties.batchSize()).thenReturn(customBatchSize);
        var completedProcesses = createCompletedProcessExecutions(15);

        when(processPersistence.findAllFullyCompleted(customBatchSize)).thenReturn(completedProcesses);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // When
        adminService.doCompaction();
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run(); // Execute the captured task

        // Then
        verify(processPersistence).findAllFullyCompleted(customBatchSize);
        verify(historyPersistence).save(completedProcesses);
    }

    @Test
    @DisplayName("Should continue processing until batch size threshold not reached")
    void shouldContinueProcessingUntilBatchSizeThresholdNotReached() {
        // Given
        when(compactionProperties.batchSize()).thenReturn(BATCH_SIZE);
        var fullBatch = createCompletedProcessExecutions(BATCH_SIZE);
        var partialBatch = createCompletedProcessExecutions(3);

        when(processPersistence.findAllFullyCompleted(BATCH_SIZE))
                .thenReturn(fullBatch)
                .thenReturn(partialBatch);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // When
        adminService.doCompaction();
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run(); // Execute the captured task

        // Then
        verify(processPersistence, times(2)).findAllFullyCompleted(BATCH_SIZE);
        verify(historyPersistence).save(fullBatch);
        verify(historyPersistence).save(partialBatch);
    }

    private List<ProcessExecution> createCompletedProcessExecutions(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> createProcessExecution("process-" + i))
                .toList();
    }

    private ProcessExecution createProcessExecution(String processId) {
        var definition = ProcessDefinition.builder()
                .id("test-definition")
                .name("Test Process")
                .version(1)
                .build();

        return ProcessExecution.builder()
                .id(processId)
                .definition(definition)
                .state(ProcessState.COMPLETED)
                .build();
    }

}