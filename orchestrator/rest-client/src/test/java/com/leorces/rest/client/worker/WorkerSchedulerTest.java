package com.leorces.rest.client.worker;

import com.leorces.rest.client.handler.TaskHandler;
import com.leorces.rest.client.model.Task;
import com.leorces.rest.client.model.worker.WorkerContext;
import com.leorces.rest.client.model.worker.WorkerMetadata;
import com.leorces.rest.client.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerScheduler Tests")
class WorkerSchedulerTest {

    @Mock
    private WorkerProcessor workerProcessor;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    private WorkerScheduler workerScheduler;

    @BeforeEach
    void setUp() {
        workerScheduler = new WorkerScheduler(workerProcessor, scheduledExecutorService);
    }

    @Test
    @DisplayName("Should schedule worker with correct initial delay and interval")
    void shouldScheduleWorkerWithCorrectInitialDelayAndInterval() {
        //Given
        var handler = new TestTaskHandler();
        var metadata = new WorkerMetadata("testTopic", "testProcess", 10L, 5L, 2, TimeUnit.SECONDS);
        var context = WorkerContext.create(handler, metadata);

        var expectedInitialDelayMillis = TimeUnit.SECONDS.toMillis(5L);
        var expectedIntervalMillis = TimeUnit.SECONDS.toMillis(10L);

        //When
        workerScheduler.startWorker(context);

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(expectedInitialDelayMillis),
                eq(expectedIntervalMillis),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should schedule worker with different time units correctly")
    void shouldScheduleWorkerWithDifferentTimeUnitsCorrectly() {
        //Given
        var handler = new TestTaskHandler();
        var metadata = new WorkerMetadata("testTopic", "testProcess", 2L, 1L, 1, TimeUnit.MINUTES);
        var context = WorkerContext.create(handler, metadata);

        var expectedInitialDelayMillis = TimeUnit.MINUTES.toMillis(1L);
        var expectedIntervalMillis = TimeUnit.MINUTES.toMillis(2L);

        //When
        workerScheduler.startWorker(context);

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(expectedInitialDelayMillis),
                eq(expectedIntervalMillis),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should handle zero initial delay correctly")
    void shouldHandleZeroInitialDelayCorrectly() {
        //Given
        var handler = new TestTaskHandler();
        var metadata = new WorkerMetadata("testTopic", "testProcess", 15L, 0L, 3, TimeUnit.SECONDS);
        var context = WorkerContext.create(handler, metadata);

        var expectedInitialDelayMillis = 0L;
        var expectedIntervalMillis = TimeUnit.SECONDS.toMillis(15L);

        //When
        workerScheduler.startWorker(context);

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(expectedInitialDelayMillis),
                eq(expectedIntervalMillis),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should execute processor when scheduled task runs and not shutting down")
    void shouldExecuteProcessorWhenScheduledTaskRunsAndNotShuttingDown() {
        //Given
        var handler = new TestTaskHandler();
        var metadata = new WorkerMetadata("testTopic", "testProcess", 5L, 0L, 1, TimeUnit.SECONDS);
        var context = WorkerContext.create(handler, metadata);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        //When
        workerScheduler.startWorker(context);

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                runnableCaptor.capture(), any(Long.class), any(Long.class), eq(TimeUnit.MILLISECONDS)
        );

        // Execute the captured runnable
        var capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        verify(workerProcessor).process(context);
    }

    @Test
    @DisplayName("Should not execute processor when shutting down")
    void shouldNotExecuteProcessorWhenShuttingDown() {
        //Given
        var handler = new TestTaskHandler();
        var metadata = new WorkerMetadata("testTopic", "testProcess", 5L, 0L, 1, TimeUnit.SECONDS);
        var context = WorkerContext.create(handler, metadata);

        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        //When
        workerScheduler.startWorker(context);
        workerScheduler.shutdown(); // Trigger shutdown first

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                runnableCaptor.capture(), any(Long.class), any(Long.class), eq(TimeUnit.MILLISECONDS)
        );

        // Execute the captured runnable after shutdown
        var capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        verifyNoInteractions(workerProcessor);
    }

    @Test
    @DisplayName("Should shutdown executor service when shutdown is called")
    void shouldShutdownExecutorServiceWhenShutdownIsCalled() {
        //When
        workerScheduler.shutdown();

        //Then
        verify(scheduledExecutorService).shutdown();
    }

    @Test
    @DisplayName("Should attempt graceful shutdown and force shutdown if needed")
    void shouldAttemptGracefulShutdownAndForceShutdownIfNeeded() throws InterruptedException {
        //Given
        when(scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(false);

        //When
        workerScheduler.shutdown();

        //Then
        var inOrder = inOrder(scheduledExecutorService);
        inOrder.verify(scheduledExecutorService).shutdown();
        inOrder.verify(scheduledExecutorService).awaitTermination(30, TimeUnit.SECONDS);
        inOrder.verify(scheduledExecutorService).shutdownNow();
    }

    @Test
    @DisplayName("Should not force shutdown if graceful shutdown succeeds")
    void shouldNotForceShutdownIfGracefulShutdownSucceeds() throws InterruptedException {
        //Given
        when(scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(true);

        //When
        workerScheduler.shutdown();

        //Then
        var inOrder = inOrder(scheduledExecutorService);
        inOrder.verify(scheduledExecutorService).shutdown();
        inOrder.verify(scheduledExecutorService).awaitTermination(30, TimeUnit.SECONDS);
        inOrder.verify(scheduledExecutorService, never()).shutdownNow();
    }

    @Test
    @DisplayName("Should handle InterruptedException during shutdown gracefully")
    void shouldHandleInterruptedExceptionDuringShutdownGracefully() throws InterruptedException {
        //Given
        when(scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("Test interruption"));

        //When
        workerScheduler.shutdown();

        //Then
        var inOrder = inOrder(scheduledExecutorService);
        inOrder.verify(scheduledExecutorService).shutdown();
        inOrder.verify(scheduledExecutorService).awaitTermination(30, TimeUnit.SECONDS);
        inOrder.verify(scheduledExecutorService).shutdownNow();

        // Verify that the current thread's interrupt status is restored
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        // Clear interrupt status for clean test state
        Thread.interrupted();
    }

    @Test
    @DisplayName("Should schedule multiple workers independently")
    void shouldScheduleMultipleWorkersIndependently() {
        //Given
        var handler1 = new TestTaskHandler();
        var metadata1 = new WorkerMetadata("topic1", "process1", 5L, 1L, 1, TimeUnit.SECONDS);
        var context1 = WorkerContext.create(handler1, metadata1);

        var handler2 = new TestTaskHandler();
        var metadata2 = new WorkerMetadata("topic2", "process2", 10L, 2L, 2, TimeUnit.MINUTES);
        var context2 = WorkerContext.create(handler2, metadata2);

        //When
        workerScheduler.startWorker(context1);
        workerScheduler.startWorker(context2);

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(TimeUnit.SECONDS.toMillis(1L)),
                eq(TimeUnit.SECONDS.toMillis(5L)),
                eq(TimeUnit.MILLISECONDS)
        );

        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(TimeUnit.MINUTES.toMillis(2L)),
                eq(TimeUnit.MINUTES.toMillis(10L)),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should convert time units to milliseconds correctly")
    void shouldConvertTimeUnitsToMillisecondsCorrectly() {
        //Given
        var handler = new TestTaskHandler();
        var metadata = new WorkerMetadata("testTopic", "testProcess", 1L, 30L, 1, TimeUnit.HOURS);
        var context = WorkerContext.create(handler, metadata);

        var expectedInitialDelayMillis = TimeUnit.HOURS.toMillis(30L);
        var expectedIntervalMillis = TimeUnit.HOURS.toMillis(1L);

        //When
        workerScheduler.startWorker(context);

        //Then
        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(expectedInitialDelayMillis),
                eq(expectedIntervalMillis),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    // Test implementation of TaskHandler
    static class TestTaskHandler implements TaskHandler {
        @Override
        public void handle(Task task, TaskService taskService) {
            // Test implementation - do nothing
        }
    }
}