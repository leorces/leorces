package com.leorces.engine.service.activity;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.GetProcessVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CallActivityService {

    private final CommandDispatcher dispatcher;
    private final ExpressionEvaluator expressionEvaluator;

    public Map<String, Object> getInputMappings(ActivityExecution activity, Map<String, Object> variables) {
        var callActivity = (CallActivity) activity.definition();
        var mappings = callActivity.inputMappings();
        return mappings != null && !mappings.isEmpty()
                ? resolveMappings(mappings, variables, callActivity.shouldProcessAllInputMappings())
                : Map.of();
    }

    public Map<String, Object> getOutputMappings(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();
        var mappings = callActivity.outputMappings();
        if (mappings == null) {
            return Map.of();
        }
        var processVariables = dispatcher.execute(GetProcessVariablesCommand.of(activity.id()));
        return resolveMappings(mappings, processVariables, callActivity.shouldProcessAllOutputMappings());
    }

    private Map<String, Object> resolveMappings(List<VariableMapping> mappings,
                                                Map<String, Object> sourceVariables,
                                                boolean shouldIncludeAll) {
        if (mappings == null || mappings.isEmpty()) {
            return Map.of();
        }
        var result = shouldIncludeAll ? new HashMap<>(sourceVariables) : new HashMap<String, Object>();
        mappings.forEach(mapping -> processVariableMapping(mapping, sourceVariables, result));
        return result;
    }

    private void processVariableMapping(VariableMapping mapping,
                                        Map<String, Object> sourceVariables,
                                        Map<String, Object> targetVariables) {
        var target = mapping.target();
        if (target == null) {
            return;
        }
        if (mapping.source() != null) {
            targetVariables.put(target, sourceVariables.get(mapping.source()));
        } else if (mapping.sourceExpression() != null) {
            var value = expressionEvaluator.evaluate(mapping.sourceExpression(), sourceVariables, Object.class);
            targetVariables.put(target, value);
        }
    }

}
