package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.process.complete.CompleteProcessEventAsync;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessCompleteService Tests")
class ProcessCompleteServiceTest {

    private static final String PROCESS_ID = "test-process-id";
    private static final String CALL_ACTIVITY_PROCESS_ID = "call-activity-process-id";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private EngineEventBus eventBus;

    private ProcessCompleteService processCompleteService;

    @BeforeEach
    void setUp() {
        processCompleteService = new ProcessCompleteService(processPersistence, activityPersistence, eventBus);
    }

    @Test
    @DisplayName("Should handle complete process event and complete process")
    void shouldHandleCompleteProcessEventAndCompleteProcess() {
        // Given
        var process = createProcess();
        var event = new CompleteProcessEventAsync(process);
        var completedProcess = createCompletedProcess();

        when(activityPersistence.isAllCompleted(PROCESS_ID)).thenReturn(true);
        when(processPersistence.complete(process)).thenReturn(completedProcess);

        // When
        processCompleteService.onApplicationEvent(event);

        // Then
        verify(activityPersistence).isAllCompleted(PROCESS_ID);
        verify(processPersistence).complete(process);
    }

    @Test
    @DisplayName("Should complete process when all activities are completed")
    void shouldCompleteProcessWhenAllActivitiesAreCompleted() {
        // Given
        var process = createProcess();
        var completedProcess = createCompletedProcess();

        when(activityPersistence.isAllCompleted(PROCESS_ID)).thenReturn(true);
        when(processPersistence.complete(process)).thenReturn(completedProcess);

        // When
        processCompleteService.complete(process);

        // Then
        verify(activityPersistence).isAllCompleted(PROCESS_ID);
        verify(processPersistence).complete(process);
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should not complete process when not all activities are completed")
    void shouldNotCompleteProcessWhenNotAllActivitiesAreCompleted() {
        // Given
        var process = createProcess();

        when(activityPersistence.isAllCompleted(PROCESS_ID)).thenReturn(false);

        // When
        processCompleteService.complete(process);

        // Then
        verify(activityPersistence).isAllCompleted(PROCESS_ID);
        verify(processPersistence, never()).complete(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should complete call activity process and publish activity complete event")
    void shouldCompleteCallActivityProcessAndPublishActivityCompleteEvent() {
        // Given
        var process = createCallActivityProcess();
        var completedProcess = createCompletedCallActivityProcess();

        when(activityPersistence.isAllCompleted(CALL_ACTIVITY_PROCESS_ID)).thenReturn(true);
        when(processPersistence.complete(process)).thenReturn(completedProcess);

        // When
        processCompleteService.complete(process);

        // Then
        verify(activityPersistence).isAllCompleted(CALL_ACTIVITY_PROCESS_ID);
        verify(processPersistence).complete(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should not publish activity event for non-call activity process")
    void shouldNotPublishActivityEventForNonCallActivityProcess() {
        // Given
        var process = createProcess();
        var completedProcess = createCompletedProcess();

        when(activityPersistence.isAllCompleted(PROCESS_ID)).thenReturn(true);
        when(processPersistence.complete(process)).thenReturn(completedProcess);

        // When
        processCompleteService.complete(process);

        // Then
        verify(activityPersistence).isAllCompleted(PROCESS_ID);
        verify(processPersistence).complete(process);
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should handle process completion with partial activity completion")
    void shouldHandleProcessCompletionWithPartialActivityCompletion() {
        // Given
        var process = createProcess();

        when(activityPersistence.isAllCompleted(PROCESS_ID)).thenReturn(false);

        // When
        processCompleteService.complete(process);

        // Then
        verify(activityPersistence).isAllCompleted(PROCESS_ID);
        verify(processPersistence, never()).complete(any());
        verify(eventBus, never()).publish(any());
    }

    private Process createProcess() {
        var definition = ProcessDefinition.builder()
                .id("test-definition")
                .name("Test Process")
                .version(1)
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private Process createCompletedProcess() {
        var definition = ProcessDefinition.builder()
                .id("test-definition")
                .name("Test Process")
                .version(1)
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .definition(definition)
                .state(ProcessState.COMPLETED)
                .build();
    }

    private Process createCallActivityProcess() {
        var definition = ProcessDefinition.builder()
                .id("call-activity-definition")
                .name("Call Activity Process")
                .version(1)
                .build();

        return Process.builder()
                .id(CALL_ACTIVITY_PROCESS_ID)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .parentId("parent-process-id")
                .build();
    }

    private Process createCompletedCallActivityProcess() {
        var definition = ProcessDefinition.builder()
                .id("call-activity-definition")
                .name("Call Activity Process")
                .version(1)
                .build();

        return Process.builder()
                .id(CALL_ACTIVITY_PROCESS_ID)
                .definition(definition)
                .state(ProcessState.COMPLETED)
                .parentId("parent-process-id")
                .build();
    }

}