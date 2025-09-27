package com.leorces.engine.correlation;


import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.correlation.CorrelateErrorEvent;
import com.leorces.engine.event.process.ProcessEvent;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ErrorActivityDefinition;
import com.leorces.model.definition.activity.event.ErrorBoundaryEvent;
import com.leorces.model.definition.activity.event.ErrorEndEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@DisplayName("ErrorCorrelationService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ErrorCorrelationServiceTest {

    private static final String ERROR_CODE = "ERROR_001";
    private static final String PROCESS_ID = "test-process-id";
    private static final String PARENT_PROCESS_ID = "parent-process-id";
    private static final String ATTACHED_TO_REF = "attached-to-ref";
    private static final String PARENT_ID = "parent-id";

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private EngineEventBus eventBus;

    @Mock
    private ActivityExecution errorEndActivity;

    @Mock
    private ErrorEndEvent errorEndEvent;

    @Mock
    private Process process;

    @Mock
    private Process parentProcess;

    @Mock
    private ProcessDefinition processDefinition;

    @Mock
    private ProcessDefinition parentProcessDefinition;

    @Mock
    private ErrorBoundaryEvent errorBoundaryEvent;

    @Mock
    private ErrorActivityDefinition errorStartEvent;

    @Mock
    private ActivityExecution callActivity;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    private ErrorCorrelationService errorCorrelationService;

    @BeforeEach
    void setUp() {
        errorCorrelationService = new ErrorCorrelationService(activityPersistence, eventBus);
    }

    @Test
    @DisplayName("Should handle error event and correlate successfully")
    void shouldHandleErrorEventAndCorrelateSuccessfully() {
        //Given
        when(errorEndActivity.process()).thenReturn(process);
        when(process.parentId()).thenReturn(null);
        setupErrorEndActivity();
        setupProcessWithActivities(List.of());
        when(errorEndActivity.scope()).thenReturn(List.of("unmatched-id"));

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus).publish(any(ApplicationEvent.class));
    }

    @Test
    @DisplayName("Should trigger error boundary event when found in scope")
    void shouldTriggerErrorBoundaryEventWhenFoundInScope() {
        //Given
        var scope = List.of(ATTACHED_TO_REF, "other-id");
        var activities = List.of((ActivityDefinition) errorBoundaryEvent);

        setupErrorEndActivity();
        setupProcessWithActivities(activities);
        setupErrorBoundaryEvent();

        when(errorEndActivity.scope()).thenReturn(scope);

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(ActivityEvent.class);
    }

    @Test
    @DisplayName("Should trigger error start event when boundary event not found")
    void shouldTriggerErrorStartEventWhenBoundaryEventNotFound() {
        //Given
        var scope = List.of(PARENT_ID);
        var activities = List.of((ActivityDefinition) errorStartEvent);

        setupErrorEndActivity();
        setupProcessWithActivities(activities);
        setupErrorStartEvent();

        when(errorEndActivity.scope()).thenReturn(scope);

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(ActivityEvent.class);
    }

    @Test
    @DisplayName("Should create process incident when no error handler found and no parent process")
    void shouldCreateProcessIncidentWhenNoErrorHandlerFoundAndNoParentProcess() {
        //Given
        var scope = List.of("unmatched-id");
        var activities = List.<ActivityDefinition>of();

        setupErrorEndActivity();
        setupProcessWithActivities(activities);

        when(errorEndActivity.scope()).thenReturn(scope);
        when(process.parentId()).thenReturn(null);

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(ProcessEvent.class);
    }

    @Test
    @DisplayName("Should correlate in parent process when current process has parent")
    void shouldCorrelateInParentProcessWhenCurrentProcessHasParent() {
        //Given
        var scope = List.of("unmatched-id");
        var activities = List.<ActivityDefinition>of();
        var parentActivities = List.of((ActivityDefinition) errorBoundaryEvent);

        setupErrorEndActivity();
        setupProcessWithActivities(activities);
        setupCallActivityForParentCorrelation(parentActivities);

        when(errorEndActivity.scope()).thenReturn(scope);
        when(process.parentId()).thenReturn(PARENT_PROCESS_ID);
        when(process.isCallActivity()).thenReturn(true);
        when(process.id()).thenReturn(PROCESS_ID);

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus, times(2)).publish(any(ApplicationEvent.class));
        verify(activityPersistence).findById(PROCESS_ID);
    }

    @Test
    @DisplayName("Should terminate call activity and create incident when no handler in parent")
    void shouldTerminateCallActivityAndCreateIncidentWhenNoHandlerInParent() {
        //Given
        var scope = List.of("unmatched-id");
        var activities = List.<ActivityDefinition>of();
        var parentActivities = List.<ActivityDefinition>of();

        setupErrorEndActivity();
        setupProcessWithActivities(activities);
        setupCallActivityForParentCorrelation(parentActivities);

        when(errorEndActivity.scope()).thenReturn(scope);
        when(process.parentId()).thenReturn(PARENT_PROCESS_ID);
        when(process.isCallActivity()).thenReturn(true, false);
        when(process.id()).thenReturn(PROCESS_ID);

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus, times(2)).publish(any(ApplicationEvent.class));
        verify(activityPersistence).findById(PROCESS_ID);
    }

    @Test
    @DisplayName("Should terminate call activity when error start event found in parent")
    void shouldTerminateCallActivityWhenErrorStartEventFoundInParent() {
        //Given
        var scope = List.of("unmatched-id");
        var activities = List.<ActivityDefinition>of();
        var parentScope = List.of(PARENT_ID);
        var parentActivities = List.of((ActivityDefinition) errorStartEvent);

        setupErrorEndActivity();
        setupProcessWithActivities(activities);
        setupCallActivityForParentCorrelation(parentActivities);
        setupErrorStartEvent();

        when(errorEndActivity.scope()).thenReturn(scope);
        when(process.parentId()).thenReturn(PARENT_PROCESS_ID);
        when(process.isCallActivity()).thenReturn(true);
        when(process.id()).thenReturn(PROCESS_ID);
        when(callActivity.scope()).thenReturn(parentScope);
        when(errorStartEvent.type()).thenReturn(ActivityType.ERROR_START_EVENT);

        //When
        errorCorrelationService.handleError(new CorrelateErrorEvent(errorEndActivity));

        //Then
        verify(eventBus, times(2)).publish(any(ApplicationEvent.class));
        verify(activityPersistence).findById(PROCESS_ID);
    }

    private void setupErrorEndActivity() {
        when(errorEndActivity.definition()).thenReturn(errorEndEvent);
        when(errorEndActivity.process()).thenReturn(process);
        when(errorEndEvent.errorCode()).thenReturn(ERROR_CODE);
    }

    private void setupProcessWithActivities(List<ActivityDefinition> activities) {
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.activities()).thenReturn(activities);
    }

    private void setupErrorBoundaryEvent() {
        when(errorBoundaryEvent.type()).thenReturn(ActivityType.ERROR_BOUNDARY_EVENT);
        when(errorBoundaryEvent.attachedToRef()).thenReturn(ATTACHED_TO_REF);
        when(errorBoundaryEvent.errorCode()).thenReturn(ERROR_CODE);
    }

    private void setupErrorStartEvent() {
        when(errorStartEvent.type()).thenReturn(ActivityType.ERROR_START_EVENT);
        when(errorStartEvent.errorCode()).thenReturn(ERROR_CODE);
        when(errorStartEvent.parentId()).thenReturn(PARENT_ID);
    }

    private void setupCallActivityForParentCorrelation(List<ActivityDefinition> parentActivities) {
        when(activityPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(callActivity));
        when(callActivity.process()).thenReturn(parentProcess);
        when(parentProcess.definition()).thenReturn(parentProcessDefinition);
        when(parentProcessDefinition.activities()).thenReturn(parentActivities);
    }

}