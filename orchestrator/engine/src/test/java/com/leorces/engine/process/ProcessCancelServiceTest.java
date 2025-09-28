package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.process.cancel.CancelProcessByIdEvent;
import com.leorces.engine.event.process.terminate.TerminateProcessByIdEvent;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessCancelService Tests")
class ProcessCancelServiceTest {

    private static final String PROCESS_ID = "test-process-id";
    private static final String NONEXISTENT_PROCESS_ID = "nonexistent-process-id";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private EngineEventBus eventBus;

    @Mock
    private ProcessMetrics processMetrics;

    private ProcessCancelService processCancelService;

    @BeforeEach
    void setUp() {
        processCancelService = new ProcessCancelService(
                processPersistence,
                eventBus,
                processMetrics
        );
    }

    @Test
    @DisplayName("Should handle cancel process event when process exists")
    void shouldHandleCancelProcessEventWhenProcessExists() {
        // Given
        var process = createProcess();
        var event = new CancelProcessByIdEvent(PROCESS_ID);

        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));

        // When
        processCancelService.handleCancel(event);

        // Then
        verify(processPersistence).findById(PROCESS_ID);
        verify(eventBus).publish(any());
        verify(processPersistence).cancel(process);
    }

    @Test
    @DisplayName("Should handle cancel process event when process does not exist")
    void shouldHandleCancelProcessEventWhenProcessDoesNotExist() {
        // Given
        var event = new CancelProcessByIdEvent(NONEXISTENT_PROCESS_ID);

        when(processPersistence.findById(NONEXISTENT_PROCESS_ID)).thenReturn(Optional.empty());

        // When
        processCancelService.handleCancel(event);

        // Then
        verify(processPersistence).findById(NONEXISTENT_PROCESS_ID);
        verify(eventBus, never()).publish(any());
        verify(processPersistence, never()).cancel(any());
    }

    @Test
    @DisplayName("Should handle terminate process event when process exists")
    void shouldHandleTerminateProcessEventWhenProcessExists() {
        // Given
        var process = createProcess();
        var event = new TerminateProcessByIdEvent(PROCESS_ID);

        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));

        // When
        processCancelService.handleTerminate(event);

        // Then
        verify(processPersistence).findById(PROCESS_ID);
        verify(eventBus).publish(any());
        verify(processPersistence).terminate(process);
    }

    @Test
    @DisplayName("Should handle terminate process event when process does not exist")
    void shouldHandleTerminateProcessEventWhenProcessDoesNotExist() {
        // Given
        var event = new TerminateProcessByIdEvent(NONEXISTENT_PROCESS_ID);

        when(processPersistence.findById(NONEXISTENT_PROCESS_ID)).thenReturn(Optional.empty());

        // When
        processCancelService.handleTerminate(event);

        // Then
        verify(processPersistence).findById(NONEXISTENT_PROCESS_ID);
        verify(eventBus, never()).publish(any());
        verify(processPersistence, never()).terminate(any());
    }

    @Test
    @DisplayName("Should cancel process and publish activity cancel event")
    void shouldCancelProcessAndPublishActivityCancelEvent() {
        // Given
        var process = createProcess();
        var event = new CancelProcessByIdEvent(PROCESS_ID);

        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));

        // When
        processCancelService.handleCancel(event);

        // Then
        verify(eventBus).publish(any());
        verify(processPersistence).cancel(process);
    }

    @Test
    @DisplayName("Should terminate process and publish activity terminate event")
    void shouldTerminateProcessAndPublishActivityTerminateEvent() {
        // Given
        var process = createProcess();
        var event = new TerminateProcessByIdEvent(PROCESS_ID);

        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));

        // When
        processCancelService.handleTerminate(event);

        // Then
        verify(eventBus).publish(any());
        verify(processPersistence).terminate(process);
    }

    private Process createProcess() {
        return Process.builder()
                .id(PROCESS_ID)
                .build();
    }

}