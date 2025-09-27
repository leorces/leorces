package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.process.start.StartProcessByCallActivityEvent;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessStartService Tests")
class ProcessStartServiceTest {

    private static final String DEFINITION_ID = "test-definition-id";
    private static final String DEFINITION_KEY = "test-definition-key";
    private static final String BUSINESS_KEY = "test-business-key";
    private static final String PROCESS_ID = "test-process-id";
    private static final String ACTIVITY_ID = "start-activity-id";
    private static final String ACTIVITY_DEFINITION_ID = "start-activity-def-id";

    @Mock
    private ProcessFactory processFactory;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private EngineEventBus eventBus;

    private ProcessStartService processStartService;

    @BeforeEach
    void setUp() {
        processStartService = new ProcessStartService(processFactory, processPersistence, eventBus);
    }

    @Test
    @DisplayName("Should start process by definition ID successfully")
    void shouldStartProcessByDefinitionIdSuccessfully() {
        // Given
        var variables = createVariablesMap();
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processFactory.createByDefinitionId(DEFINITION_ID, BUSINESS_KEY, variables)).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        var result = processStartService.startByDefinitionId(DEFINITION_ID, BUSINESS_KEY, variables);

        // Then
        assertThat(result).isEqualTo(startedProcess);
        verify(processFactory).createByDefinitionId(DEFINITION_ID, BUSINESS_KEY, variables);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should start process by definition key successfully")
    void shouldStartProcessByDefinitionKeySuccessfully() {
        // Given
        var variables = createVariablesMap();
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processFactory.createByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, variables)).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        var result = processStartService.startByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, variables);

        // Then
        assertThat(result).isEqualTo(startedProcess);
        verify(processFactory).createByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, variables);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle start process by call activity event")
    void shouldHandleStartProcessByCallActivityEvent() {
        // Given
        var activity = createActivityExecution();
        var event = new StartProcessByCallActivityEvent(activity);
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processFactory.createByCallActivity(activity)).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        processStartService.handleStart(event);

        // Then
        verify(processFactory).createByCallActivity(activity);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should start process and trigger initial activity")
    void shouldStartProcessAndTriggerInitialActivity() {
        // Given
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        var result = processStartService.start(process);

        // Then
        assertThat(result).isEqualTo(startedProcess);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when start activity not found")
    void shouldThrowExceptionWhenStartActivityNotFound() {
        // Given
        var process = createProcessWithoutStartActivity();
        var startedProcess = createStartedProcessWithoutStartActivity();

        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When & Then
        assertThatThrownBy(() -> processStartService.start(process))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(processPersistence).run(process);
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should start process with empty variables")
    void shouldStartProcessWithEmptyVariables() {
        // Given
        var emptyVariables = new HashMap<String, Object>();
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processFactory.createByDefinitionId(DEFINITION_ID, BUSINESS_KEY, emptyVariables)).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        var result = processStartService.startByDefinitionId(DEFINITION_ID, BUSINESS_KEY, emptyVariables);

        // Then
        assertThat(result).isEqualTo(startedProcess);
        verify(processFactory).createByDefinitionId(DEFINITION_ID, BUSINESS_KEY, emptyVariables);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should start process with null business key")
    void shouldStartProcessWithNullBusinessKey() {
        // Given
        var variables = createVariablesMap();
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processFactory.createByDefinitionKey(DEFINITION_KEY, null, variables)).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        var result = processStartService.startByDefinitionKey(DEFINITION_KEY, null, variables);

        // Then
        assertThat(result).isEqualTo(startedProcess);
        verify(processFactory).createByDefinitionKey(DEFINITION_KEY, null, variables);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should handle multiple start activities and choose the first one")
    void shouldHandleMultipleStartActivitiesAndChooseTheFirstOne() {
        // Given
        var process = createProcessWithMultipleStartActivities();
        var startedProcess = createStartedProcessWithMultipleStartActivities();

        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        var result = processStartService.start(process);

        // Then
        assertThat(result).isEqualTo(startedProcess);
        verify(processPersistence).run(process);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should publish correct activity event with process and activity definition")
    void shouldPublishCorrectActivityEventWithProcessAndActivityDefinition() {
        // Given
        var process = createProcess();
        var startedProcess = createStartedProcess();

        when(processPersistence.run(process)).thenReturn(startedProcess);

        // When
        processStartService.start(process);

        // Then
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    private Process createProcess() {
        var startActivity = createStartActivityDefinition();
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(startActivity))
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private Process createStartedProcess() {
        var startActivity = createStartActivityDefinition();
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(startActivity))
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private Process createProcessWithoutStartActivity() {
        var normalActivity = createNormalActivityDefinition();
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(normalActivity))
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private Process createStartedProcessWithoutStartActivity() {
        var normalActivity = createNormalActivityDefinition();
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(normalActivity))
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private Process createProcessWithMultipleStartActivities() {
        var firstStartActivity = createStartActivityDefinition();
        var secondStartActivity = createSecondStartActivityDefinition();
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(firstStartActivity, secondStartActivity))
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private Process createStartedProcessWithMultipleStartActivities() {
        var firstStartActivity = createStartActivityDefinition();
        var secondStartActivity = createSecondStartActivityDefinition();
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(firstStartActivity, secondStartActivity))
                .build();

        return Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .build();
    }

    private ActivityDefinition createStartActivityDefinition() {
        var mockActivity = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_DEFINITION_ID);
        when(mockActivity.type()).thenReturn(ActivityType.START_EVENT);
        return mockActivity;
    }

    private ActivityDefinition createNormalActivityDefinition() {
        var mockActivity = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn("normal-activity");
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        return mockActivity;
    }

    private ActivityDefinition createSecondStartActivityDefinition() {
        var mockActivity = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn("second-start-activity");
        when(mockActivity.type()).thenReturn(ActivityType.START_EVENT);
        return mockActivity;
    }

    private ActivityExecution createActivityExecution() {
        var mockProcess = mock(Process.class, withSettings().lenient());
        when(mockProcess.id()).thenReturn(PROCESS_ID);

        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.process()).thenReturn(mockProcess);
        return mockActivity;
    }

    private Map<String, Object> createVariablesMap() {
        var variables = new HashMap<String, Object>();
        variables.put("testVar", "testValue");
        return variables;
    }

}