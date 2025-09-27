package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.event.activity.trigger.TriggerActivityByDefinitionEventAsync;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityTriggerService Tests")
class ActivityTriggerServiceTest {

    private static final String DEFINITION_ID = "test-definition-id";
    private static final String PROCESS_ID = "test-process-id";

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private TriggerableActivityBehaviour triggerableBehaviour;

    private ActivityTriggerService activityTriggerService;

    @BeforeEach
    void setUp() {
        activityTriggerService = new ActivityTriggerService(behaviorResolver);
    }

    @Test
    @DisplayName("Should handle trigger activity by definition event successfully")
    void shouldHandleTriggerActivityByDefinitionEventSuccessfully() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(definition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verify(triggerableBehaviour).trigger(process, definition);
    }

    @Test
    @DisplayName("Should handle triggerable strategy not found gracefully")
    void shouldHandleTriggerableStrategyNotFoundGracefully() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(definition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.empty());

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verify(triggerableBehaviour, never()).trigger(any(), any());
    }

    @Test
    @DisplayName("Should handle different activity types for triggering")
    void shouldHandleDifferentActivityTypesForTriggering() {
        // Given
        var messageEventDefinition = createActivityDefinitionWithType(ActivityType.INTERMEDIATE_CATCH_EVENT);
        var timerEventDefinition = createActivityDefinitionWithType(ActivityType.START_EVENT);
        var process = createProcess();
        var messageEvent = new TriggerActivityByDefinitionEventAsync(messageEventDefinition, process);
        var timerEvent = new TriggerActivityByDefinitionEventAsync(timerEventDefinition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));
        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.START_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(messageEvent);
        activityTriggerService.handleTrigger(timerEvent);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.START_EVENT);
        verify(triggerableBehaviour).trigger(process, messageEventDefinition);
        verify(triggerableBehaviour).trigger(process, timerEventDefinition);
    }

    @Test
    @DisplayName("Should pass correct process and definition to triggerable behaviour")
    void shouldPassCorrectProcessAndDefinitionToTriggerableBehaviour() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(definition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        verify(triggerableBehaviour).trigger(process, definition);
    }

    @Test
    @DisplayName("Should handle multiple trigger events for same process")
    void shouldHandleMultipleTriggerEventsForSameProcess() {
        // Given
        var definition1 = createActivityDefinition();
        var definition2 = createActivityDefinitionWithId("another-definition-id");
        var process = createProcess();
        var event1 = new TriggerActivityByDefinitionEventAsync(definition1, process);
        var event2 = new TriggerActivityByDefinitionEventAsync(definition2, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event1);
        activityTriggerService.handleTrigger(event2);

        // Then
        verify(behaviorResolver, times(2)).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verify(triggerableBehaviour).trigger(process, definition1);
        verify(triggerableBehaviour).trigger(process, definition2);
    }

    @Test
    @DisplayName("Should handle trigger events for different processes")
    void shouldHandleTriggerEventsForDifferentProcesses() {
        // Given
        var definition = createActivityDefinition();
        var process1 = createProcess();
        var process2 = createProcessWithId("different-process-id");
        var event1 = new TriggerActivityByDefinitionEventAsync(definition, process1);
        var event2 = new TriggerActivityByDefinitionEventAsync(definition, process2);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event1);
        activityTriggerService.handleTrigger(event2);

        // Then
        verify(behaviorResolver, times(2)).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verify(triggerableBehaviour).trigger(process1, definition);
        verify(triggerableBehaviour).trigger(process2, definition);
    }

    @Test
    @DisplayName("Should verify behavior resolver is called with correct activity type")
    void shouldVerifyBehaviorResolverIsCalledWithCorrectActivityType() {
        // Given
        var startEventDefinition = createActivityDefinitionWithType(ActivityType.START_EVENT);
        var catchEventDefinition = createActivityDefinitionWithType(ActivityType.INTERMEDIATE_CATCH_EVENT);
        var process = createProcess();
        var startEvent = new TriggerActivityByDefinitionEventAsync(startEventDefinition, process);
        var catchEvent = new TriggerActivityByDefinitionEventAsync(catchEventDefinition, process);

        when(behaviorResolver.resolveTriggerableStrategy(any())).thenReturn(Optional.empty());

        // When
        activityTriggerService.handleTrigger(startEvent);
        activityTriggerService.handleTrigger(catchEvent);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.START_EVENT);
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
    }

    @Test
    @DisplayName("Should handle end event triggering")
    void shouldHandleEndEventTriggering() {
        // Given
        var endEventDefinition = createActivityDefinitionWithType(ActivityType.END_EVENT);
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(endEventDefinition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.END_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.END_EVENT);
        verify(triggerableBehaviour).trigger(process, endEventDefinition);
    }

    @Test
    @DisplayName("Should handle external task triggering")
    void shouldHandleExternalTaskTriggering() {
        // Given
        var taskDefinition = createActivityDefinitionWithType(ActivityType.EXTERNAL_TASK);
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(taskDefinition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.EXTERNAL_TASK);
        verify(triggerableBehaviour).trigger(process, taskDefinition);
    }

    @Test
    @DisplayName("Should verify no interactions when strategy not found")
    void shouldVerifyNoInteractionsWhenStrategyNotFound() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(definition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.empty());

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verifyNoInteractions(triggerableBehaviour);
    }

    @Test
    @DisplayName("Should handle async event processing correctly")
    void shouldHandleAsyncEventProcessingCorrectly() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();
        var event = new TriggerActivityByDefinitionEventAsync(definition, process);

        when(behaviorResolver.resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT))
                .thenReturn(Optional.of(triggerableBehaviour));

        // When
        activityTriggerService.handleTrigger(event);

        // Then
        // Verify that the async annotation is handled (this would be tested by integration tests)
        verify(behaviorResolver).resolveTriggerableStrategy(ActivityType.INTERMEDIATE_CATCH_EVENT);
        verify(triggerableBehaviour).trigger(process, definition);
    }

    private ActivityDefinition createActivityDefinition() {
        var mockDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockDefinition.id()).thenReturn(DEFINITION_ID);
        when(mockDefinition.type()).thenReturn(ActivityType.INTERMEDIATE_CATCH_EVENT);
        return mockDefinition;
    }

    private ActivityDefinition createActivityDefinitionWithId(String id) {
        var mockDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockDefinition.id()).thenReturn(id);
        when(mockDefinition.type()).thenReturn(ActivityType.INTERMEDIATE_CATCH_EVENT);
        return mockDefinition;
    }

    private ActivityDefinition createActivityDefinitionWithType(ActivityType type) {
        var mockDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockDefinition.id()).thenReturn(DEFINITION_ID);
        when(mockDefinition.type()).thenReturn(type);
        return mockDefinition;
    }

    private Process createProcess() {
        var mockProcess = mock(Process.class, withSettings().lenient());
        when(mockProcess.id()).thenReturn(PROCESS_ID);
        when(mockProcess.state()).thenReturn(ProcessState.ACTIVE);
        return mockProcess;
    }

    private Process createProcessWithId(String id) {
        var mockProcess = mock(Process.class, withSettings().lenient());
        when(mockProcess.id()).thenReturn(id);
        when(mockProcess.state()).thenReturn(ProcessState.ACTIVE);
        return mockProcess;
    }

}