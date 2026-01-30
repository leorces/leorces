package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.GetCallActivityMappingsCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.variables.command.GetProcessVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetCallActivityMappingsCommandHandler implements ResultCommandHandler<GetCallActivityMappingsCommand, Map<String, Object>> {

    private final CommandDispatcher dispatcher;
    private final ExpressionEvaluator expressionEvaluator;

    @Override
    public Map<String, Object> execute(GetCallActivityMappingsCommand command) {
        return command.type() == GetCallActivityMappingsCommand.MappingType.INPUT
                ? getInputMappings(command)
                : getOutputMappings(command);
    }

    @Override
    public Class<GetCallActivityMappingsCommand> getCommandType() {
        return GetCallActivityMappingsCommand.class;
    }

    private Map<String, Object> getInputMappings(GetCallActivityMappingsCommand command) {
        var callActivity = (CallActivity) command.activity().definition();
        var mappings = callActivity.inputMappings();
        return mappings != null && !mappings.isEmpty()
                ? resolveMappings(mappings, command.variables(), callActivity.shouldProcessAllInputMappings())
                : Map.of();
    }

    private Map<String, Object> getOutputMappings(GetCallActivityMappingsCommand command) {
        var activity = command.activity();
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
