package com.leorces.engine.process;

import com.leorces.engine.exception.process.ProcessDefinitionNotFoundException;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessFactory Tests")
class ProcessFactoryTest {

    private static final String DEFINITION_ID = "test-definition-id";
    private static final String DEFINITION_KEY = "test-definition-key";
    private static final String BUSINESS_KEY = "test-business-key";
    private static final String VARIABLE_KEY = "testVar";
    private static final String VARIABLE_VALUE = "testValue";
    private static final String ACTIVITY_ID = "call-activity-id";
    private static final String PARENT_PROCESS_ID = "parent-process-id";
    private static final String ROOT_PROCESS_ID = "root-process-id";
    private static final Integer VERSION = 1;

    @Mock
    private DefinitionPersistence definitionPersistence;

    @Mock
    private VariableRuntimeService variableRuntimeService;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    private ProcessFactory processFactory;

    @BeforeEach
    void setUp() {
        processFactory = new ProcessFactory(definitionPersistence, variableRuntimeService, expressionEvaluator);
    }

    @Test
    @DisplayName("Should create process by definition ID successfully")
    void shouldCreateProcessByDefinitionIdSuccessfully() {
        // Given
        var definition = createProcessDefinition(DEFINITION_ID);
        var variables = createVariablesMap();
        var variablesList = List.of(createVariable());

        when(definitionPersistence.findById(DEFINITION_ID)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.toList(variables)).thenReturn(variablesList);

        // When
        var result = processFactory.createByDefinitionId(DEFINITION_ID, BUSINESS_KEY, variables);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.businessKey()).isEqualTo(BUSINESS_KEY);
        assertThat(result.variables()).isEqualTo(variablesList);
        assertThat(result.definition()).isEqualTo(definition);
        verify(definitionPersistence).findById(DEFINITION_ID);
        verify(variableRuntimeService).toList(variables);
    }

    @Test
    @DisplayName("Should throw exception when definition not found by ID")
    void shouldThrowExceptionWhenDefinitionNotFoundById() {
        // Given
        var variables = createVariablesMap();

        when(definitionPersistence.findById(DEFINITION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> processFactory.createByDefinitionId(DEFINITION_ID, BUSINESS_KEY, variables))
                .isInstanceOf(ProcessDefinitionNotFoundException.class);

        verify(definitionPersistence).findById(DEFINITION_ID);
        verify(variableRuntimeService, never()).toList(any());
    }

    @Test
    @DisplayName("Should create process by definition key successfully")
    void shouldCreateProcessByDefinitionKeySuccessfully() {
        // Given
        var definition = createProcessDefinition(DEFINITION_ID);
        var variables = createVariablesMap();
        var variablesList = List.of(createVariable());

        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.toList(variables)).thenReturn(variablesList);

        // When
        var result = processFactory.createByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, variables);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.businessKey()).isEqualTo(BUSINESS_KEY);
        assertThat(result.variables()).isEqualTo(variablesList);
        assertThat(result.definition()).isEqualTo(definition);
        verify(definitionPersistence).findLatestByKey(DEFINITION_KEY);
        verify(variableRuntimeService).toList(variables);
    }

    @Test
    @DisplayName("Should throw exception when definition not found by key")
    void shouldThrowExceptionWhenDefinitionNotFoundByKey() {
        // Given
        var variables = createVariablesMap();

        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> processFactory.createByDefinitionKey(DEFINITION_KEY, BUSINESS_KEY, variables))
                .isInstanceOf(ProcessDefinitionNotFoundException.class);

        verify(definitionPersistence).findLatestByKey(DEFINITION_KEY);
        verify(variableRuntimeService, never()).toList(any());
    }

    @Test
    @DisplayName("Should create process by call activity with inherit variables")
    void shouldCreateProcessByCallActivityWithInheritVariables() {
        // Given
        var activity = createActivityExecution();
        var callActivity = createCallActivity(true, List.of());
        var definition = createProcessDefinition("called-definition");
        var scopedVariables = createVariablesMap();
        var variablesList = List.of(createVariable());

        when(activity.definition()).thenReturn(callActivity);
        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.getScopedVariables(activity)).thenReturn(scopedVariables);
        when(variableRuntimeService.toList(scopedVariables)).thenReturn(variablesList);

        // When
        var result = processFactory.createByCallActivity(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(ACTIVITY_ID);
        assertThat(result.parentId()).isEqualTo(PARENT_PROCESS_ID);
        assertThat(result.rootProcessId()).isEqualTo(ROOT_PROCESS_ID);
        assertThat(result.businessKey()).isEqualTo(BUSINESS_KEY);
        assertThat(result.definition()).isEqualTo(definition);
        assertThat(result.variables()).isEqualTo(variablesList);
        verify(variableRuntimeService).getScopedVariables(activity);
        verify(variableRuntimeService).toList(scopedVariables);
    }

    @Test
    @DisplayName("Should create process by call activity without inherit variables")
    void shouldCreateProcessByCallActivityWithoutInheritVariables() {
        // Given
        var activity = createActivityExecution();
        var callActivity = createCallActivity(false, List.of());
        var definition = createProcessDefinition("called-definition");
        var scopedVariables = createVariablesMap();
        var emptyVariables = new HashMap<String, Object>();
        var variablesList = List.of(createVariable());

        when(activity.definition()).thenReturn(callActivity);
        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.getScopedVariables(activity)).thenReturn(scopedVariables);
        when(variableRuntimeService.toList(emptyVariables)).thenReturn(variablesList);

        // When
        var result = processFactory.createByCallActivity(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(ACTIVITY_ID);
        assertThat(result.parentId()).isEqualTo(PARENT_PROCESS_ID);
        assertThat(result.businessKey()).isEqualTo(BUSINESS_KEY);
        assertThat(result.definition()).isEqualTo(definition);
        verify(variableRuntimeService).getScopedVariables(activity);
        verify(variableRuntimeService).toList(emptyVariables);
    }

    @Test
    @DisplayName("Should create process by call activity with input mappings")
    void shouldCreateProcessByCallActivityWithInputMappings() {
        // Given
        var activity = createActivityExecution();
        var inputMapping = VariableMapping.builder()
                .source("sourceVar")
                .target("targetVar")
                .build();
        var callActivity = createCallActivity(false, List.of(inputMapping));
        var definition = createProcessDefinition("called-definition");
        var scopedVariables = new HashMap<String, Object>();
        scopedVariables.put("sourceVar", "sourceValue");
        var expectedVariables = new HashMap<String, Object>();
        expectedVariables.put("targetVar", "sourceValue");
        var variablesList = List.of(createVariable());

        when(activity.definition()).thenReturn(callActivity);
        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.getScopedVariables(activity)).thenReturn(scopedVariables);
        when(variableRuntimeService.toList(expectedVariables)).thenReturn(variablesList);

        // When
        var result = processFactory.createByCallActivity(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.variables()).isEqualTo(variablesList);
        verify(variableRuntimeService).getScopedVariables(activity);
        verify(variableRuntimeService).toList(expectedVariables);
    }

    @Test
    @DisplayName("Should create process by call activity with expression mapping")
    void shouldCreateProcessByCallActivityWithExpressionMapping() {
        // Given
        var activity = createActivityExecution();
        var inputMapping = VariableMapping.builder()
                .sourceExpression("${sourceVar + 10}")
                .target("targetVar")
                .build();
        var callActivity = createCallActivity(false, List.of(inputMapping));
        var definition = createProcessDefinition("called-definition");
        var scopedVariables = new HashMap<String, Object>();
        scopedVariables.put("sourceVar", 5);
        var expectedVariables = new HashMap<String, Object>();
        expectedVariables.put("targetVar", 15);
        var variablesList = List.of(createVariable());

        when(activity.definition()).thenReturn(callActivity);
        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.getScopedVariables(activity)).thenReturn(scopedVariables);
        when(expressionEvaluator.evaluate("${sourceVar + 10}", scopedVariables, Object.class)).thenReturn(15);
        when(variableRuntimeService.toList(expectedVariables)).thenReturn(variablesList);

        // When
        var result = processFactory.createByCallActivity(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.variables()).isEqualTo(variablesList);
        verify(expressionEvaluator).evaluate("${sourceVar + 10}", scopedVariables, Object.class);
        verify(variableRuntimeService).toList(expectedVariables);
    }

    @Test
    @DisplayName("Should handle process without root process ID")
    void shouldHandleProcessWithoutRootProcessId() {
        // Given
        var parentProcess = Process.builder()
                .id(PARENT_PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .rootProcessId(null)
                .build();
        var activity = createActivityExecutionWithProcess(parentProcess);
        var callActivity = createCallActivity(false, List.of());
        var definition = createProcessDefinition("called-definition");
        var variablesList = List.of(createVariable());

        when(activity.definition()).thenReturn(callActivity);
        when(activity.process()).thenReturn(parentProcess);
        when(definitionPersistence.findLatestByKey(DEFINITION_KEY)).thenReturn(Optional.of(definition));
        when(variableRuntimeService.getScopedVariables(activity)).thenReturn(new HashMap<>());
        when(variableRuntimeService.toList(any())).thenReturn(variablesList);

        // When
        var result = processFactory.createByCallActivity(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.rootProcessId()).isEqualTo(PARENT_PROCESS_ID);
    }

    private ProcessDefinition createProcessDefinition(String id) {
        return ProcessDefinition.builder()
                .id(id)
                .key(DEFINITION_KEY)
                .name("Test Process")
                .version(VERSION)
                .build();
    }

    private Map<String, Object> createVariablesMap() {
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_KEY, VARIABLE_VALUE);
        return variables;
    }

    private Variable createVariable() {
        return Variable.builder()
                .varKey(VARIABLE_KEY)
                .varValue(VARIABLE_VALUE)
                .build();
    }

    private ActivityExecution createActivityExecution() {
        var process = Process.builder()
                .id(PARENT_PROCESS_ID)
                .rootProcessId(ROOT_PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .state(ProcessState.ACTIVE)
                .build();

        return createActivityExecutionWithProcess(process);
    }

    private ActivityExecution createActivityExecutionWithProcess(Process process) {
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.process()).thenReturn(process);
        return mockActivity;
    }

    private CallActivity createCallActivity(boolean inheritVariables, List<VariableMapping> inputMappings) {
        var mockCallActivity = mock(CallActivity.class, withSettings().lenient());
        when(mockCallActivity.calledElement()).thenReturn(DEFINITION_KEY);
        when(mockCallActivity.calledElementVersion()).thenReturn(null);
        when(mockCallActivity.inheritVariables()).thenReturn(inheritVariables);
        when(mockCallActivity.inputMappings()).thenReturn(inputMappings);
        return mockCallActivity;
    }

}