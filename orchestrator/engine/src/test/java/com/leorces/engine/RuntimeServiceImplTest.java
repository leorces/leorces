package com.leorces.engine;


import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.correlation.CorrelateMessageEvent;
import com.leorces.engine.process.ProcessStartService;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("RuntimeServiceImpl Unit Tests")
@ExtendWith(MockitoExtension.class)
class RuntimeServiceImplTest {

    private static final String DEFINITION_ID = "test-definition-id";
    private static final String DEFINITION_KEY = "test-definition-key";
    private static final String BUSINESS_KEY = "test-business-key";
    private static final String EXECUTION_ID = "test-execution-id";
    private static final String MESSAGE_NAME = "test-message";
    private static final String VARIABLE_KEY = "testKey";
    private static final String VARIABLE_VALUE = "testValue";
    private static final Map<String, Object> VARIABLES = Map.of(
            "key1", "value1",
            "key2", 42,
            "key3", true
    );
    private static final Map<String, Object> CORRELATION_KEYS = Map.of(
            "correlationKey1", "correlationValue1",
            "correlationKey2", 123
    );

    @Mock
    private VariableRuntimeService variableRuntimeService;

    @Mock
    private ProcessStartService processStartService;

    @Mock
    private EngineEventBus eventBus;

    @Mock
    private ProcessDefinition processDefinition;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    private RuntimeServiceImpl runtimeService;

    @BeforeEach
    void setUp() {
        runtimeService = new RuntimeServiceImpl(variableRuntimeService, processStartService, eventBus);
    }

    @Test
    @DisplayName("Should start process by definition id only")
    void shouldStartProcessByDefinitionIdOnly() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionId(DEFINITION_ID, null, Collections.emptyMap()))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionId(DEFINITION_ID, null, Collections.emptyMap());
    }

    @Test
    @DisplayName("Should start process by definition id with variables")
    void shouldStartProcessByDefinitionIdWithVariables() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionId(DEFINITION_ID, null, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID, VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionId(DEFINITION_ID, null, VARIABLES);
    }

    @Test
    @DisplayName("Should start process by definition id with business key")
    void shouldStartProcessByDefinitionIdWithBusinessKey() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionId(DEFINITION_ID, BUSINESS_KEY, Collections.emptyMap()))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID, BUSINESS_KEY);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionId(DEFINITION_ID, BUSINESS_KEY, Collections.emptyMap());
    }

    @Test
    @DisplayName("Should start process by definition id with business key and variables")
    void shouldStartProcessByDefinitionIdWithBusinessKeyAndVariables() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionId(DEFINITION_ID, BUSINESS_KEY, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessById(DEFINITION_ID, BUSINESS_KEY, VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionId(DEFINITION_ID, BUSINESS_KEY, VARIABLES);
    }

    @Test
    @DisplayName("Should start process by key only")
    void shouldStartProcessByKeyOnly() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionKey(DEFINITION_KEY, null, Collections.emptyMap()))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(DEFINITION_KEY);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionKey(DEFINITION_KEY, null, Collections.emptyMap());
    }

    @Test
    @DisplayName("Should start process by key with variables")
    void shouldStartProcessByKeyWithVariables() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionKey(DEFINITION_KEY, null, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(DEFINITION_KEY, VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionKey(DEFINITION_KEY, null, VARIABLES);
    }

    @Test
    @DisplayName("Should start process by key with business key")
    void shouldStartProcessByKeyWithBusinessKey() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, Collections.emptyMap()))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(DEFINITION_KEY, BUSINESS_KEY);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, Collections.emptyMap());
    }

    @Test
    @DisplayName("Should start process by key with business key and variables")
    void shouldStartProcessByKeyWithBusinessKeyAndVariables() {
        //Given
        var expectedProcess = createTestProcess();
        when(processStartService.startByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, VARIABLES))
                .thenReturn(expectedProcess);

        //When
        var result = runtimeService.startProcessByKey(DEFINITION_KEY, BUSINESS_KEY, VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedProcess);
        verify(processStartService).startByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, VARIABLES);
    }

    @Test
    @DisplayName("Should set single variable")
    void shouldSetSingleVariable() {
        //Given
        var expectedVariableMap = Collections.<String, Object>singletonMap(VARIABLE_KEY, VARIABLE_VALUE);

        //When
        runtimeService.setVariable(EXECUTION_ID, VARIABLE_KEY, VARIABLE_VALUE);

        //Then
        verify(variableRuntimeService).setVariables(EXECUTION_ID, expectedVariableMap);
    }

    @Test
    @DisplayName("Should set multiple variables")
    void shouldSetMultipleVariables() {
        //When
        runtimeService.setVariables(EXECUTION_ID, VARIABLES);

        //Then
        verify(variableRuntimeService).setVariables(EXECUTION_ID, VARIABLES);
    }

    @Test
    @DisplayName("Should set single local variable")
    void shouldSetSingleLocalVariable() {
        //Given
        var expectedVariableMap = Collections.<String, Object>singletonMap(VARIABLE_KEY, VARIABLE_VALUE);

        //When
        runtimeService.setVariableLocal(EXECUTION_ID, VARIABLE_KEY, VARIABLE_VALUE);

        //Then
        verify(variableRuntimeService).setVariablesLocal(EXECUTION_ID, expectedVariableMap);
    }

    @Test
    @DisplayName("Should set multiple local variables")
    void shouldSetMultipleLocalVariables() {
        //When
        runtimeService.setVariablesLocal(EXECUTION_ID, VARIABLES);

        //Then
        verify(variableRuntimeService).setVariablesLocal(EXECUTION_ID, VARIABLES);
    }

    @Test
    @DisplayName("Should correlate message with business key only")
    void shouldCorrelateMessageWithBusinessKeyOnly() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, BUSINESS_KEY);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CorrelateMessageEvent.class);

        var correlateEvent = (CorrelateMessageEvent) capturedEvent;
        assertThat(correlateEvent.messageName).isEqualTo(MESSAGE_NAME);
        assertThat(correlateEvent.businessKey).isEqualTo(BUSINESS_KEY);
        assertThat(correlateEvent.correlationKeys).isEmpty();
        assertThat(correlateEvent.processVariables).isEmpty();
    }

    @Test
    @DisplayName("Should correlate message with correlation keys only")
    void shouldCorrelateMessageWithCorrelationKeysOnly() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, CORRELATION_KEYS);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CorrelateMessageEvent.class);

        var correlateEvent = (CorrelateMessageEvent) capturedEvent;
        assertThat(correlateEvent.messageName).isEqualTo(MESSAGE_NAME);
        assertThat(correlateEvent.businessKey).isNull();
        assertThat(correlateEvent.correlationKeys).isEqualTo(CORRELATION_KEYS);
        assertThat(correlateEvent.processVariables).isEmpty();
    }

    @Test
    @DisplayName("Should correlate message with business key and process variables")
    void shouldCorrelateMessageWithBusinessKeyAndProcessVariables() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, BUSINESS_KEY, VARIABLES);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CorrelateMessageEvent.class);

        var correlateEvent = (CorrelateMessageEvent) capturedEvent;
        assertThat(correlateEvent.messageName).isEqualTo(MESSAGE_NAME);
        assertThat(correlateEvent.businessKey).isEqualTo(BUSINESS_KEY);
        assertThat(correlateEvent.correlationKeys).isEmpty();
        assertThat(correlateEvent.processVariables).isEqualTo(VARIABLES);
    }

    @Test
    @DisplayName("Should correlate message with correlation keys and process variables")
    void shouldCorrelateMessageWithCorrelationKeysAndProcessVariables() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, CORRELATION_KEYS, VARIABLES);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CorrelateMessageEvent.class);

        var correlateEvent = (CorrelateMessageEvent) capturedEvent;
        assertThat(correlateEvent.messageName).isEqualTo(MESSAGE_NAME);
        assertThat(correlateEvent.businessKey).isNull();
        assertThat(correlateEvent.correlationKeys).isEqualTo(CORRELATION_KEYS);
        assertThat(correlateEvent.processVariables).isEqualTo(VARIABLES);
    }

    @Test
    @DisplayName("Should correlate message with all parameters")
    void shouldCorrelateMessageWithAllParameters() {
        //When
        runtimeService.correlateMessage(MESSAGE_NAME, BUSINESS_KEY, CORRELATION_KEYS, VARIABLES);

        //Then
        verify(eventBus).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(CorrelateMessageEvent.class);

        var correlateEvent = (CorrelateMessageEvent) capturedEvent;
        assertThat(correlateEvent.messageName).isEqualTo(MESSAGE_NAME);
        assertThat(correlateEvent.businessKey).isEqualTo(BUSINESS_KEY);
        assertThat(correlateEvent.correlationKeys).isEqualTo(CORRELATION_KEYS);
        assertThat(correlateEvent.processVariables).isEqualTo(VARIABLES);
    }

    private Process createTestProcess() {
        return Process.builder()
                .id("process-id")
                .businessKey(BUSINESS_KEY)
                .state(ProcessState.ACTIVE)
                .definition(processDefinition)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();
    }

}