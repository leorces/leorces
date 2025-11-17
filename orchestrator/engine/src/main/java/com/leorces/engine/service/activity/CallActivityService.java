package com.leorces.engine.service.activity;

import com.leorces.engine.service.variable.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CallActivityService {

    private final VariablesService variablesService;
    private final ExpressionEvaluator expressionEvaluator;

    public Map<String, Object> getInputMappings(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();

        if (callActivity.inputMappings() == null) {
            return Collections.emptyMap();
        }

        var inputMappings = callActivity.inputMappings();

        var scopedVariables = variablesService.getScopedVariables(activity);
        return resolveMappings(
                inputMappings,
                scopedVariables,
                callActivity.shouldProcessAllInputMappings()
        );
    }

    public Map<String, Object> getOutputMappings(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();

        if (callActivity.outputMappings() == null) {
            return Collections.emptyMap();
        }

        var outputMappings = callActivity.outputMappings();

        var processVariables = variablesService.getProcessVariables(activity.id());
        return resolveMappings(
                outputMappings,
                processVariables,
                callActivity.shouldProcessAllOutputMappings()
        );
    }

    private Map<String, Object> resolveMappings(List<VariableMapping> mappings,
                                                Map<String, Object> sourceVars,
                                                boolean includeAll) {
        if (mappings == null || mappings.isEmpty()) {
            return Collections.emptyMap();
        }

        var result = new HashMap<String, Object>();

        if (includeAll) {
            result.putAll(sourceVars);
        }

        for (var mapping : mappings) {
            processVariableMapping(mapping, sourceVars, result);
        }

        return result;
    }

    private void processVariableMapping(VariableMapping mapping,
                                        Map<String, Object> sourceVars,
                                        Map<String, Object> targetVars) {
        var target = mapping.target();
        if (target == null) {
            return;
        }

        if (mapping.source() != null) {
            targetVars.put(target, sourceVars.get(mapping.source()));
            return;
        }

        if (mapping.sourceExpression() != null) {
            var value = expressionEvaluator.evaluate(mapping.sourceExpression(), sourceVars, Object.class);
            targetVars.put(target, value);
        }
    }

}
