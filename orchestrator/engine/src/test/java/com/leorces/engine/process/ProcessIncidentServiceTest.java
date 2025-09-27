package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.activity.complete.CompleteActivitySuccessEvent;
import com.leorces.engine.event.activity.fail.IncidentFailActivityEvent;
import com.leorces.engine.event.process.incident.IncidentProcessEventAsync;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
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
@DisplayName("ProcessIncidentService Tests")
class ProcessIncidentServiceTest {

    private static final String PROCESS_ID = "test-process-id";
    private static final String CALL_ACTIVITY_PROCESS_ID = "call-activity-process-id";
    private static final String ACTIVITY_ID = "test-activity-id";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private EngineEventBus eventBus;

    private ProcessIncidentService processIncidentService;

    @BeforeEach
    void setUp() {
        processIncidentService = new ProcessIncidentService(processPersistence, activityPersistence, eventBus);
    }

    @Test
    @DisplayName("Should handle incident process event and create incident")
    void shouldHandleIncidentProcessEventAndCreateIncident() {
        // Given
        var process = createProcess();
        var event = new IncidentProcessEventAsync(process);
        var incidentProcess = createIncidentProcess();

        when(processPersistence.incident(process)).thenReturn(incidentProcess);

        // When
        processIncidentService.handleIncident(event);

        // Then
        verify(processPersistence).incident(process);
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should handle incident fail activity event and create incident")
    void shouldHandleIncidentFailActivityEventAndCreateIncident() {
        // Given
        var activity = createActivityExecution();
        var process = createProcess();
        var event = new IncidentFailActivityEvent(activity);
        var incidentProcess = createIncidentProcess();

        when(activity.process()).thenReturn(process);
        when(processPersistence.incident(process)).thenReturn(incidentProcess);

        // When
        processIncidentService.handleIncident(event);

        // Then
        verify(processPersistence).incident(process);
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should create incident for call activity process and publish fail event")
    void shouldCreateIncidentForCallActivityProcessAndPublishFailEvent() {
        // Given
        var process = createCallActivityProcess();
        var event = new IncidentProcessEventAsync(process);
        var incidentProcess = createIncidentCallActivityProcess();

        when(processPersistence.incident(process)).thenReturn(incidentProcess);

        // When
        processIncidentService.handleIncident(event);

        // Then
        verify(processPersistence).incident(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should not publish fail event for non-call activity process")
    void shouldNotPublishFailEventForNonCallActivityProcess() {
        // Given
        var process = createProcess();
        var event = new IncidentProcessEventAsync(process);
        var incidentProcess = createIncidentProcess();

        when(processPersistence.incident(process)).thenReturn(incidentProcess);

        // When
        processIncidentService.handleIncident(event);

        // Then
        verify(processPersistence).incident(process);
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should handle complete activity success event for incident process")
    void shouldHandleCompleteActivitySuccessEventForIncidentProcess() {
        // Given
        var activity = createActivityExecution();
        var incidentProcess = createIncidentProcess();
        var event = new CompleteActivitySuccessEvent(activity);

        when(activity.process()).thenReturn(incidentProcess);
        when(activityPersistence.isAnyFailed(PROCESS_ID)).thenReturn(false);

        // When
        processIncidentService.handle(event);

        // Then
        verify(activityPersistence).isAnyFailed(PROCESS_ID);
        verify(processPersistence).changeState(PROCESS_ID, ProcessState.ACTIVE);
    }

    @Test
    @DisplayName("Should not recover process when activities are still failed")
    void shouldNotRecoverProcessWhenActivitiesAreStillFailed() {
        // Given
        var activity = createActivityExecution();
        var incidentProcess = createIncidentProcess();
        var event = new CompleteActivitySuccessEvent(activity);

        when(activity.process()).thenReturn(incidentProcess);
        when(activityPersistence.isAnyFailed(PROCESS_ID)).thenReturn(true);

        // When
        processIncidentService.handle(event);

        // Then
        verify(activityPersistence).isAnyFailed(PROCESS_ID);
        verify(processPersistence, never()).changeState(anyString(), any());
    }

    @Test
    @DisplayName("Should not process complete activity event for non-incident process")
    void shouldNotProcessCompleteActivityEventForNonIncidentProcess() {
        // Given
        var activity = createActivityExecution();
        var activeProcess = createProcess();
        var event = new CompleteActivitySuccessEvent(activity);

        when(activity.process()).thenReturn(activeProcess);

        // When
        processIncidentService.handle(event);

        // Then
        verify(activityPersistence, never()).isAnyFailed(anyString());
        verify(processPersistence, never()).changeState(anyString(), any());
    }

    @Test
    @DisplayName("Should handle incident from fail activity event with call activity")
    void shouldHandleIncidentFromFailActivityEventWithCallActivity() {
        // Given
        var activity = createActivityExecution();
        var callActivityProcess = createCallActivityProcess();
        var event = new IncidentFailActivityEvent(activity);
        var incidentProcess = createIncidentCallActivityProcess();

        when(activity.process()).thenReturn(callActivityProcess);
        when(processPersistence.incident(callActivityProcess)).thenReturn(incidentProcess);

        // When
        processIncidentService.handleIncident(event);

        // Then
        verify(processPersistence).incident(callActivityProcess);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should recover incident process when no failed activities remain")
    void shouldRecoverIncidentProcessWhenNoFailedActivitiesRemain() {
        // Given
        var activity = createActivityExecution();
        var incidentProcess = createIncidentProcess();
        var event = new CompleteActivitySuccessEvent(activity);

        when(activity.process()).thenReturn(incidentProcess);
        when(activityPersistence.isAnyFailed(PROCESS_ID)).thenReturn(false);

        // When
        processIncidentService.handle(event);

        // Then
        verify(activityPersistence).isAnyFailed(PROCESS_ID);
        verify(processPersistence).changeState(PROCESS_ID, ProcessState.ACTIVE);
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

    private Process createIncidentProcess() {
        var definition = ProcessDefinition.builder()
                .id("test-definition")
                .name("Test Process")
                .version(1)
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .definition(definition)
                .state(ProcessState.INCIDENT)
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

    private Process createIncidentCallActivityProcess() {
        var definition = ProcessDefinition.builder()
                .id("call-activity-definition")
                .name("Call Activity Process")
                .version(1)
                .build();

        return Process.builder()
                .id(CALL_ACTIVITY_PROCESS_ID)
                .definition(definition)
                .state(ProcessState.INCIDENT)
                .parentId("parent-process-id")
                .build();
    }

    private ActivityExecution createActivityExecution() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.state()).thenReturn(ActivityState.ACTIVE);
        when(mockActivity.process()).thenReturn(process);
        return mockActivity;
    }

}