package com.leorces.engine.correlation;


import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.correlation.CorrelateMessageEvent;
import com.leorces.engine.exception.correlation.MessageCorrelationException;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@DisplayName("MessageCorrelationService Unit Tests")
@ExtendWith(MockitoExtension.class)
class MessageCorrelationServiceTest {

    private static final String MESSAGE_NAME = "TestMessage";
    private static final String BUSINESS_KEY = "test-business-key";
    private static final String PROCESS_ID = "test-process-id";
    private static final Map<String, Object> CORRELATION_KEYS = Map.of(
            "correlationKey1", "value1",
            "correlationKey2", 42
    );
    private static final Map<String, Object> PROCESS_VARIABLES = Map.of(
            "var1", "value1",
            "var2", 100,
            "var3", true
    );
    private static final Map<String, Object> EMPTY_MAP = Map.of();

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private VariableRuntimeService variableRuntimeService;

    @Mock
    private EngineEventBus eventBus;

    @Mock
    private Process process;

    @Mock
    private Process secondProcess;

    @Mock
    private ProcessDefinition processDefinition;

    @Mock
    private ProcessState processState;

    @Mock
    private MessageActivityDefinition messageActivityDefinition;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    private MessageCorrelationService messageCorrelationService;

    @BeforeEach
    void setUp() {
        messageCorrelationService = new MessageCorrelationService(
                processPersistence,
                variableRuntimeService,
                eventBus
        );
    }

    @Test
    @DisplayName("Should handle message event and correlate successfully")
    void shouldHandleMessageEventAndCorrelateSuccessfully() {
        //Given
        var correlateMessageEvent = new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, CORRELATION_KEYS, PROCESS_VARIABLES);
        var processes = List.of(process);
        var activities = List.of((ActivityDefinition) messageActivityDefinition);

        setupSuccessfulCorrelation(processes, activities);

        //When
        messageCorrelationService.handleMessage(correlateMessageEvent);

        //Then
        verify(processPersistence).findByBusinessKeyAndVariables(BUSINESS_KEY, CORRELATION_KEYS);
        verify(variableRuntimeService).setProcessVariables(process, PROCESS_VARIABLES);
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should correlate message with business key and correlation keys")
    void shouldCorrelateMessageWithBusinessKeyAndCorrelationKeys() {
        //Given
        var processes = List.of(process);
        var activities = List.of((ActivityDefinition) messageActivityDefinition);

        setupSuccessfulCorrelation(processes, activities);

        //When
        messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, CORRELATION_KEYS, PROCESS_VARIABLES));

        //Then
        verify(processPersistence).findByBusinessKeyAndVariables(BUSINESS_KEY, CORRELATION_KEYS);
        verify(variableRuntimeService).setProcessVariables(process, PROCESS_VARIABLES);
        verify(eventBus).publish(eventCaptor.capture());

        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(ActivityEvent.class);
    }

    @Test
    @DisplayName("Should correlate message with business key only")
    void shouldCorrelateMessageWithBusinessKeyOnly() {
        //Given
        var processes = List.of(process);
        var activities = List.of((ActivityDefinition) messageActivityDefinition);

        setupProcessForCorrelationWithBusinessKeyOnly(processes);
        setupProcessDefinitionWithActivities(activities);
        setupMessageActivity(messageActivityDefinition);
        when(process.state()).thenReturn(processState);
        when(processState.isTerminal()).thenReturn(false);

        //When
        messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP));

        //Then
        verify(processPersistence).findByBusinessKey(BUSINESS_KEY);
        verify(variableRuntimeService, never()).setProcessVariables(any(), any());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should correlate message with correlation keys only")
    void shouldCorrelateMessageWithCorrelationKeysOnly() {
        //Given
        var processes = List.of(process);
        var activities = List.of((ActivityDefinition) messageActivityDefinition);

        setupProcessForCorrelationWithVariablesOnly(processes);
        setupProcessDefinitionWithActivities(activities);
        setupMessageActivity(messageActivityDefinition);
        when(process.state()).thenReturn(processState);
        when(processState.isTerminal()).thenReturn(false);

        //When
        messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, "", CORRELATION_KEYS, EMPTY_MAP));

        //Then
        verify(processPersistence).findByVariables(CORRELATION_KEYS);
        verify(variableRuntimeService, never()).setProcessVariables(any(), any());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when no business key and correlation keys provided")
    void shouldThrowExceptionWhenNoBusinessKeyAndCorrelationKeysProvided() {
        //When & Then
        assertThatThrownBy(() ->
                messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, "", EMPTY_MAP, EMPTY_MAP))
        ).isInstanceOf(MessageCorrelationException.class);

        verify(processPersistence, never()).findByBusinessKey(any());
        verify(processPersistence, never()).findByVariables(any());
        verify(processPersistence, never()).findByBusinessKeyAndVariables(any(), any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when no processes correlated")
    void shouldThrowExceptionWhenNoProcessesCorrelated() {
        //Given
        when(processPersistence.findByBusinessKey(BUSINESS_KEY)).thenReturn(List.of());

        //When & Then
        assertThatThrownBy(() ->
                messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP))
        ).isInstanceOf(MessageCorrelationException.class);

        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when multiple processes correlated")
    void shouldThrowExceptionWhenMultipleProcessesCorrelated() {
        //Given
        var processes = List.of(process, secondProcess);
        when(processPersistence.findByBusinessKey(BUSINESS_KEY)).thenReturn(processes);
        when(process.definition()).thenReturn(processDefinition);
        when(secondProcess.definition()).thenReturn(processDefinition);
        when(processDefinition.messages()).thenReturn(List.of(MESSAGE_NAME));

        //When & Then
        assertThatThrownBy(() ->
                messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP))
        ).isInstanceOf(MessageCorrelationException.class);

        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when process is in terminal state")
    void shouldThrowExceptionWhenProcessIsInTerminalState() {
        //Given
        var processes = List.of(process);
        when(processPersistence.findByBusinessKey(BUSINESS_KEY)).thenReturn(processes);
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.messages()).thenReturn(List.of(MESSAGE_NAME));
        when(process.state()).thenReturn(processState);
        when(processState.isTerminal()).thenReturn(true);
        when(process.id()).thenReturn(PROCESS_ID);

        //When & Then
        assertThatThrownBy(() ->
                messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP))
        ).isInstanceOf(MessageCorrelationException.class);

        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should not correlate when message name does not match process messages")
    void shouldNotCorrelateWhenMessageNameDoesNotMatchProcessMessages() {
        //Given
        var processes = List.of(process);
        when(processPersistence.findByBusinessKey(BUSINESS_KEY)).thenReturn(processes);
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.messages()).thenReturn(List.of("DifferentMessage"));

        //When & Then
        assertThatThrownBy(() ->
                messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP))
        ).isInstanceOf(MessageCorrelationException.class);

        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Should trigger multiple activities when multiple message activities match")
    void shouldTriggerMultipleActivitiesWhenMultipleMessageActivitiesMatch() {
        //Given
        var messageActivity2 = createMockMessageActivity();
        var processes = List.of(process);
        var activities = List.of(messageActivityDefinition, (ActivityDefinition) messageActivity2);

        setupProcessForCorrelationWithBusinessKeyOnly(processes);
        setupProcessDefinitionWithActivities(activities);
        setupMessageActivity(messageActivityDefinition);
        setupMessageActivity(messageActivity2);
        when(process.state()).thenReturn(processState);
        when(processState.isTerminal()).thenReturn(false);

        //When
        messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, EMPTY_MAP, EMPTY_MAP));

        //Then
        verify(eventBus, times(2)).publish(any(ActivityEvent.class));
    }

    @Test
    @DisplayName("Should not set variables when process variables are empty")
    void shouldNotSetVariablesWhenProcessVariablesAreEmpty() {
        //Given
        var processes = List.of(process);
        var activities = List.of((ActivityDefinition) messageActivityDefinition);

        setupSuccessfulCorrelation(processes, activities);

        //When
        messageCorrelationService.handleMessage(new CorrelateMessageEvent(MESSAGE_NAME, BUSINESS_KEY, CORRELATION_KEYS, EMPTY_MAP));

        //Then
        verify(variableRuntimeService, never()).setProcessVariables(any(), any());
        verify(eventBus).publish(any(ActivityEvent.class));
    }

    private void setupSuccessfulCorrelation(List<Process> processes, List<ActivityDefinition> activities) {
        setupProcessForCorrelationWithBusinessKeyAndVariables(processes);
        setupProcessDefinitionWithActivities(activities);
        setupMessageActivity(messageActivityDefinition);
        when(process.state()).thenReturn(processState);
        when(processState.isTerminal()).thenReturn(false);
    }

    private void setupProcessForCorrelationWithBusinessKeyAndVariables(List<Process> processes) {
        when(processPersistence.findByBusinessKeyAndVariables(BUSINESS_KEY, CORRELATION_KEYS)).thenReturn(processes);
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.messages()).thenReturn(List.of(MESSAGE_NAME));
    }

    private void setupProcessForCorrelationWithBusinessKeyOnly(List<Process> processes) {
        when(processPersistence.findByBusinessKey(BUSINESS_KEY)).thenReturn(processes);
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.messages()).thenReturn(List.of(MESSAGE_NAME));
    }

    private void setupProcessForCorrelationWithVariablesOnly(List<Process> processes) {
        when(processPersistence.findByVariables(CORRELATION_KEYS)).thenReturn(processes);
        when(process.definition()).thenReturn(processDefinition);
        when(processDefinition.messages()).thenReturn(List.of(MESSAGE_NAME));
    }

    private void setupProcessDefinitionWithActivities(List<ActivityDefinition> activities) {
        when(processDefinition.activities()).thenReturn(activities);
    }

    private void setupMessageActivity(MessageActivityDefinition activity) {
        when(activity.messageReference()).thenReturn(MessageCorrelationServiceTest.MESSAGE_NAME);
    }

    private MessageActivityDefinition createMockMessageActivity() {
        var mockActivity = org.mockito.Mockito.mock(MessageActivityDefinition.class);
        when(mockActivity.messageReference()).thenReturn(MESSAGE_NAME);
        return mockActivity;
    }

}