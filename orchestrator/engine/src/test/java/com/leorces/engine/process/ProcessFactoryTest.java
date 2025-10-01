package com.leorces.engine.process;

import com.leorces.engine.exception.process.ProcessDefinitionNotFoundException;
import com.leorces.engine.variables.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessFactoryTest Tests")
class ProcessFactoryTest {

    @Mock
    private ProcessDefinition processDefinition;

    @Mock
    private DefinitionPersistence definitionPersistence;

    @Mock
    private VariablesService variablesService;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @InjectMocks
    private ProcessFactory processFactory;

    @BeforeEach
    void setUp() {
        lenient().when(processDefinition.key()).thenReturn("order-process");
    }

    @Test
    @DisplayName("createByDefinitionId should return process with variables and definition")
    void createByDefinitionIdShouldReturnProcessWithVariablesAndDefinition() {
        Map<String, Object> variables = Map.of("var1", "value1");
        when(definitionPersistence.findById("def-1")).thenReturn(Optional.of(processDefinition));
        when(variablesService.toList(variables)).thenReturn(List.of());

        var process = processFactory.createByDefinitionId("def-1", "bk1", variables);

        assertThat(process.definition()).isEqualTo(processDefinition);
        assertThat(process.businessKey()).isEqualTo("bk1");
        verify(variablesService).toList(variables);
    }

    @Test
    @DisplayName("createByDefinitionId should throw exception when definition not found")
    void createByDefinitionIdShouldThrowWhenDefinitionNotFound() {
        when(definitionPersistence.findById("def-1")).thenReturn(Optional.empty());

        assertThrows(ProcessDefinitionNotFoundException.class,
                () -> processFactory.createByDefinitionId("def-1", "bk1", Map.of()));
    }

    @Test
    @DisplayName("createByDefinitionKey should return process with latest definition")
    void createByDefinitionKeyShouldReturnProcessWithLatestDefinition() {
        Map<String, Object> variables = Map.of("var1", "value1");
        when(definitionPersistence.findLatestByKey("order-process")).thenReturn(Optional.of(processDefinition));
        when(variablesService.toList(variables)).thenReturn(List.of());

        var process = processFactory.createByDefinitionKey("order-process", "bk1", variables);

        assertThat(process.definition()).isEqualTo(processDefinition);
        assertThat(process.businessKey()).isEqualTo("bk1");
        verify(variablesService).toList(variables);
    }

    @Test
    @DisplayName("createByDefinitionKey should throw exception when latest definition not found")
    void createByDefinitionKeyShouldThrowWhenLatestDefinitionNotFound() {
        when(definitionPersistence.findLatestByKey("order-process")).thenReturn(Optional.empty());

        assertThrows(ProcessDefinitionNotFoundException.class,
                () -> processFactory.createByDefinitionKey("order-process", "bk1", Map.of()));
    }

    @Test
    @DisplayName("createByCallActivity should inherit variables and apply mappings")
    void createByCallActivityShouldInheritVariablesAndApplyMappings() {
        // Setup activity execution and call activity
        var activityExecution = mock(ActivityExecution.class);
        var callActivity = mock(CallActivity.class);

        when(activityExecution.definition()).thenReturn(callActivity);
        when(activityExecution.process()).thenReturn(Process.builder().id("p1").businessKey("bk1").build());
        when(callActivity.calledElement()).thenReturn("order-process");
        when(callActivity.calledElementVersion()).thenReturn(null);
        when(callActivity.inheritVariables()).thenReturn(true);
        when(callActivity.inputMappings()).thenReturn(List.of());

        when(definitionPersistence.findLatestByKey("order-process")).thenReturn(Optional.of(processDefinition));
        when(variablesService.getScopedVariables(activityExecution)).thenReturn(Map.of("var1", "value1"));
        when(variablesService.toList(anyMap())).thenReturn(List.of());

        var process = processFactory.createByCallActivity(activityExecution);

        assertThat(process.definition()).isEqualTo(processDefinition);
        assertThat(process.parentId()).isEqualTo("p1");
        assertThat(process.businessKey()).isEqualTo("bk1");
        verify(variablesService).toList(Map.of("var1", "value1"));
    }

    @Test
    @DisplayName("createByCallActivity should evaluate sourceExpression and apply to target variable")
    void createByCallActivityShouldEvaluateSourceExpression() {
        // Setup activity execution and call activity
        var activityExecution = mock(ActivityExecution.class);
        var callActivity = mock(CallActivity.class);

        when(activityExecution.definition()).thenReturn(callActivity);
        when(activityExecution.process()).thenReturn(Process.builder().id("p1").businessKey("bk1").build());
        when(activityExecution.id()).thenReturn("exec1");

        when(callActivity.calledElement()).thenReturn("order-process");
        when(callActivity.calledElementVersion()).thenReturn(null);
        when(callActivity.inheritVariables()).thenReturn(false);

        // Input mapping with sourceExpression
        var mapping = mock(VariableMapping.class);
        when(mapping.sourceExpression()).thenReturn("${var1 + '_suffix'}");
        when(mapping.target()).thenReturn("mappedVar");
        when(mapping.source()).thenReturn(null);
        when(mapping.variables()).thenReturn(null);
        when(callActivity.inputMappings()).thenReturn(List.of(mapping));

        // Mock definition and variables
        when(definitionPersistence.findLatestByKey("order-process")).thenReturn(Optional.of(processDefinition));
        Map<String, Object> scopedVariables = Map.of("var1", "value1");
        when(variablesService.getScopedVariables(activityExecution)).thenReturn(scopedVariables);

        // Mock expression evaluation
        when(expressionEvaluator.evaluate("${var1 + '_suffix'}", scopedVariables, Object.class)).thenReturn("value1_suffix");
        when(variablesService.toList(anyMap())).thenReturn(List.of());

        var process = processFactory.createByCallActivity(activityExecution);

        assertThat(process.definition()).isEqualTo(processDefinition);
        assertThat(process.parentId()).isEqualTo("p1");
        assertThat(process.businessKey()).isEqualTo("bk1");

        // Verify expression evaluated and result applied
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(variablesService).toList(captor.capture());

        Map<String, Object> capturedVariables = captor.getValue();
        assertThat(capturedVariables).containsEntry("mappedVar", "value1_suffix");
    }

}
