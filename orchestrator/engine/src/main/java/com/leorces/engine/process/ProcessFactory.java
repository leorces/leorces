package com.leorces.engine.process;

import com.leorces.engine.exception.process.ProcessDefinitionNotFoundException;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
class ProcessFactory {

    private final DefinitionPersistence definitionPersistence;
    private final VariableRuntimeService variableRuntimeService;
    private final ExpressionEvaluator expressionEvaluator;

    public Process createByDefinitionId(String definitionId,
                                        String businessKey,
                                        Map<String, Object> variables) {
        var definition = definitionPersistence.findById(definitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(definitionId));

        return Process.builder()
                .businessKey(businessKey)
                .variables(variableRuntimeService.toList(variables))
                .definition(definition)
                .build();
    }

    public Process createByDefinitionKey(String definitionKey,
                                         String businessKey,
                                         Map<String, Object> variables) {
        var definition = getDefinitionByKey(definitionKey);
        return Process.builder()
                .businessKey(businessKey)
                .variables(variableRuntimeService.toList(variables))
                .definition(definition)
                .build();
    }

    public Process createByCallActivity(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();
        var definition = getDefinition(callActivity.calledElement(), callActivity.calledElementVersion());
        var variables = prepareVariables(activity);
        var rootProcessId = activity.process().rootProcessId() == null
                ? activity.process().id()
                : activity.process().rootProcessId();


        return Process.builder()
                .id(activity.id())
                .parentId(activity.process().id())
                .rootProcessId(rootProcessId)
                .businessKey(activity.process().businessKey())
                .definition(definition)
                .variables(variableRuntimeService.toList(variables))
                .build();
    }

    private Map<String, Object> prepareVariables(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();
        var variables = new HashMap<String, Object>();
        var scopedVariables = variableRuntimeService.getScopedVariables(activity);

        if (callActivity.inheritVariables()) {
            variables.putAll(scopedVariables);
        }

        // Process input mappings
        for (var mapping : callActivity.inputMappings()) {
            processVariableMapping(mapping, scopedVariables, variables);
        }

        return variables;
    }

    private void processVariableMapping(VariableMapping mapping,
                                        Map<String, Object> scopedVariables,
                                        Map<String, Object> variables) {

        if (mapping.source() != null && mapping.target() != null) {
            var sourceValue = scopedVariables.get(mapping.source());
            if (sourceValue != null) {
                variables.put(mapping.target(), sourceValue);
            }
        }

        if (mapping.sourceExpression() != null && mapping.target() != null) {
            var evaluatedValue = expressionEvaluator.evaluate(mapping.sourceExpression(), scopedVariables, Object.class);
            variables.put(mapping.target(), evaluatedValue);
        }

        if (mapping.variables() != null && "all".equals(mapping.variables())) {
            variables.putAll(scopedVariables);
        }
    }

    private ProcessDefinition getDefinition(String definitionKey, Integer version) {
        if (version == null) {
            return definitionPersistence.findLatestByKey(definitionKey)
                    .orElseThrow(() -> ProcessDefinitionNotFoundException.byKey(definitionKey));
        }
        return definitionPersistence.findByKeyAndVersion(definitionKey, version)
                .orElseThrow(() -> ProcessDefinitionNotFoundException.byKeyAndVersion(definitionKey, version));
    }

    private ProcessDefinition getDefinitionByKey(String definitionKey) {
        return definitionPersistence.findLatestByKey(definitionKey)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(definitionKey));
    }

}
