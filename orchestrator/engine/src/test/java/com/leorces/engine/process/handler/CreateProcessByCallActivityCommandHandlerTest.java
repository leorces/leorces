package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.activity.command.GetCallActivityMappingsCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CreateProcessByCallActivityCommand;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProcessByCallActivityCommandHandler Tests")
class CreateProcessByCallActivityCommandHandlerTest {

    private static final String CALLED_ELEMENT = "called-element";
    private static final String BUSINESS_KEY = "business-key";
    private static final String PROCESS_ID = "process-id";
    private static final String ACTIVITY_ID = "activity-id";
    private static final String SUB_DEF_ID = "sub-def-id";
    private static final String PARENT_DEF_ID = "parent-def-id";
    private static final String CALL_ACTIVITY_ID = "call-activity-id";
    private static final String VAR_KEY = "var1";
    private static final String VAR_VALUE = "val1";
    private static final String RESOLVED_KEY = "resolved-key";
    private static final String SUB_PROCESS_KEY = "subProcessKey";
    private static final String EXPRESSION = "${subProcessKey}";

    @Mock
    private VariablesMapper variablesMapper;
    @Mock
    private DefinitionPersistence definitionPersistence;
    @Mock
    private ExpressionEvaluator expressionEvaluator;
    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private CreateProcessByCallActivityCommandHandler handler;

    private Process parentProcess;
    private ProcessDefinition parentDefinition;
    private CallActivity callActivityDefinition;
    private ActivityExecution activity;

    @BeforeEach
    void setUp() {
        callActivityDefinition = CallActivity.builder()
                .id(CALL_ACTIVITY_ID)
                .calledElement(CALLED_ELEMENT)
                .build();

        parentDefinition = ProcessDefinition.builder()
                .id(PARENT_DEF_ID)
                .activities(List.of(callActivityDefinition))
                .build();

        parentProcess = Process.builder()
                .id(PROCESS_ID)
                .businessKey(BUSINESS_KEY)
                .definition(parentDefinition)
                .suspended(false)
                .build();

        activity = ActivityExecution.builder()
                .id(ACTIVITY_ID)
                .definitionId(callActivityDefinition.id())
                .process(parentProcess)
                .build();
    }

    @Test
    @DisplayName("Should create process with plain called element")
    void executeWithPlainCalledElement() {
        // Given
        var command = CreateProcessByCallActivityCommand.of(activity);
        var subProcessDefinition = ProcessDefinition.builder().id(SUB_DEF_ID).key(CALLED_ELEMENT).build();
        var inputMappings = Map.<String, Object>of(VAR_KEY, VAR_VALUE);

        when(expressionEvaluator.isExpression(CALLED_ELEMENT)).thenReturn(false);
        when(definitionPersistence.findLatestByKey(CALLED_ELEMENT)).thenReturn(Optional.of(subProcessDefinition));
        when(dispatcher.execute(any(GetCallActivityMappingsCommand.class))).thenReturn(inputMappings);
        when(variablesMapper.map(inputMappings)).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertCreatedProcess(result, subProcessDefinition);
        verify(definitionPersistence).findLatestByKey(CALLED_ELEMENT);
    }

    @Test
    @DisplayName("Should create process with expression called element")
    void executeWithExpressionCalledElement() {
        // Given
        var activityWithExpression = setupActivityWithExpression(EXPRESSION);
        var command = CreateProcessByCallActivityCommand.of(activityWithExpression);
        var subProcessDefinition = ProcessDefinition.builder().id(SUB_DEF_ID).key(RESOLVED_KEY).build();
        var scopedVariables = Map.<String, Object>of(SUB_PROCESS_KEY, RESOLVED_KEY);

        mockExpressionEvaluation(EXPRESSION, RESOLVED_KEY, scopedVariables, activityWithExpression);
        when(definitionPersistence.findLatestByKey(RESOLVED_KEY)).thenReturn(Optional.of(subProcessDefinition));
        when(dispatcher.execute(any(GetCallActivityMappingsCommand.class))).thenReturn(Map.of());
        when(variablesMapper.map(any())).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertCreatedProcess(result, subProcessDefinition);
        verify(expressionEvaluator).evaluateString(EXPRESSION, scopedVariables);
    }

    private ActivityExecution setupActivityWithExpression(String expression) {
        var callActivityWithExpression = callActivityDefinition.toBuilder().calledElement(expression).build();
        var parentDefWithExpression = parentDefinition.toBuilder().activities(List.of(callActivityWithExpression)).build();
        var parentProcessWithExpression = parentProcess.toBuilder().definition(parentDefWithExpression).build();
        return activity.toBuilder().process(parentProcessWithExpression).build();
    }

    private void mockExpressionEvaluation(String expression, String resolvedKey, Map<String, Object> scopedVariables, ActivityExecution activityWithExpression) {
        when(expressionEvaluator.isExpression(expression)).thenReturn(true);
        when(dispatcher.execute(GetScopedVariablesCommand.of(activityWithExpression))).thenReturn(scopedVariables);
        when(expressionEvaluator.evaluateString(expression, scopedVariables)).thenReturn(resolvedKey);
    }

    @Test
    @DisplayName("Should create process with specific version")
    void executeWithVersion() {
        // Given
        var version = 2;
        var activityWithVersion = setupActivityWithVersion(version);
        var command = CreateProcessByCallActivityCommand.of(activityWithVersion);
        var subProcessDefinition = ProcessDefinition.builder().id(SUB_DEF_ID).key(CALLED_ELEMENT).version(version).build();

        when(expressionEvaluator.isExpression(CALLED_ELEMENT)).thenReturn(false);
        when(definitionPersistence.findByKeyAndVersion(CALLED_ELEMENT, version)).thenReturn(Optional.of(subProcessDefinition));
        when(dispatcher.execute(any(GetCallActivityMappingsCommand.class))).thenReturn(Map.of());
        when(variablesMapper.map(any())).thenReturn(List.of());

        // When
        var result = handler.execute(command);

        // Then
        assertCreatedProcess(result, subProcessDefinition);
        verify(definitionPersistence).findByKeyAndVersion(CALLED_ELEMENT, version);
    }

    private ActivityExecution setupActivityWithVersion(Integer version) {
        var callActivityWithVersion = callActivityDefinition.toBuilder().calledElementVersion(version).build();
        var parentDefWithVersion = parentDefinition.toBuilder().activities(List.of(callActivityWithVersion)).build();
        var parentProcessWithVersion = parentProcess.toBuilder().definition(parentDefWithVersion).build();
        return activity.toBuilder().process(parentProcessWithVersion).build();
    }

    @Test
    @DisplayName("Should throw exception when called element is null")
    void executeWithNullCalledElement() {
        // Given
        var activityWithNull = setupActivityWithNull();
        var command = CreateProcessByCallActivityCommand.of(activityWithNull);

        // When & Then
        assertThrows(ExecutionException.class, () -> handler.execute(command));
    }

    private ActivityExecution setupActivityWithNull() {
        var callActivityWithNull = callActivityDefinition.toBuilder().calledElement(null).build();
        var parentDefWithNull = parentDefinition.toBuilder().activities(List.of(callActivityWithNull)).build();
        var parentProcessWithNull = parentProcess.toBuilder().definition(parentDefWithNull).build();
        return activity.toBuilder().process(parentProcessWithNull).build();
    }

    @Test
    @DisplayName("Should throw exception when definition not found")
    void executeWithNonExistentDefinition() {
        // Given
        var command = CreateProcessByCallActivityCommand.of(activity);
        when(expressionEvaluator.isExpression(CALLED_ELEMENT)).thenReturn(false);
        when(definitionPersistence.findLatestByKey(CALLED_ELEMENT)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExecutionException.class, () -> handler.execute(command));
    }

    private void assertCreatedProcess(Process result, ProcessDefinition definition) {
        assertNotNull(result);
        assertEquals(ACTIVITY_ID, result.id());
        assertEquals(PROCESS_ID, result.parentId());
        assertEquals(BUSINESS_KEY, result.businessKey());
        assertEquals(definition, result.definition());
    }

}
