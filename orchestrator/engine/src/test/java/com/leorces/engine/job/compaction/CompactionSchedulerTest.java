package com.leorces.engine.job.compaction;

import com.leorces.engine.configuration.properties.CompactionProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import com.leorces.engine.scheduler.ShedlockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompactionScheduler Tests")
class CompactionSchedulerTest {

    @Mock
    private ShedlockService shedlockService;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private CompactionProperties compactionProperties;

    @InjectMocks
    private CompactionScheduler scheduler;

    @Test
    @DisplayName("configureTasks should register cron task when enabled")
    void configureTasksShouldRegisterTaskWhenEnabled() {
        // Given
        var registrar = mock(ScheduledTaskRegistrar.class);
        var cron = "0 0 * * * *";
        when(compactionProperties.enabled()).thenReturn(true);
        when(compactionProperties.cron()).thenReturn(cron);

        // When
        scheduler.configureTasks(registrar);

        // Then
        verify(registrar).addCronTask(any(Runnable.class), eq(cron));
    }

    @Test
    @DisplayName("configureTasks should not register task when disabled")
    void configureTasksShouldNotRegisterTaskWhenDisabled() {
        // Given
        var registrar = mock(ScheduledTaskRegistrar.class);
        when(compactionProperties.enabled()).thenReturn(false);

        // When
        scheduler.configureTasks(registrar);

        // Then
        verifyNoInteractions(registrar);
    }

    @Test
    @DisplayName("doCompaction should execute with lock and dispatch command")
    @SuppressWarnings("unchecked")
    void doCompactionShouldExecuteWithLockAndDispatchCommand() {
        // Given
        var registrar = mock(ScheduledTaskRegistrar.class);
        when(compactionProperties.enabled()).thenReturn(true);
        when(compactionProperties.cron()).thenReturn("0 0 * * * *");

        var taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        scheduler.configureTasks(registrar);
        verify(registrar).addCronTask(taskCaptor.capture(), anyString());
        var registeredTask = taskCaptor.getValue();

        // When
        registeredTask.run();

        // Then
        var supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(shedlockService).executeWithLock(eq("compaction-job"), any(Duration.class), supplierCaptor.capture());

        // Execute the task passed to shedlock
        supplierCaptor.getValue().get();
        verify(dispatcher).dispatch(any(CompactionCommand.class));
    }

}
