package com.leorces.engine.correlation;


import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.correlation.CorrelateVariablesEvent;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ConditionalActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class VariablesCorrelationService {

    private final VariableRuntimeService variableRuntimeService;
    private final ExpressionEvaluator expressionEvaluator;
    private final EngineEventBus eventBus;

    @Async
    @EventListener
    void handleVariables(CorrelateVariablesEvent event) {
        correlate(event.process, event.variables);
    }

    private void correlate(Process process, List<Variable> variables) {
        if (variables.isEmpty()) {
            return;
        }

        var processDefinition = process.definition();
        var variablesByExecutionId = variables.stream()
                .collect(Collectors.groupingBy(Variable::executionDefinitionId));

        processDefinition.activities().stream()
                .filter(ConditionalActivityDefinition.class::isInstance)
                .map(ConditionalActivityDefinition.class::cast)
                .filter(definition -> isConditionMet(definition, processDefinition, variablesByExecutionId))
                .map(definition -> ActivityEvent.triggerByDefinitionAsync(definition, process))
                .forEach(eventBus::publish);
    }

    private boolean isConditionMet(ConditionalActivityDefinition activityDefinition,
                                   ProcessDefinition processDefinition,
                                   Map<String, List<Variable>> variablesByExecutionId) {
        var condition = activityDefinition.condition();
        var scope = processDefinition.scope(activityDefinition.id());

        var variablesInScope = scope.stream()
                .flatMap(id -> variablesByExecutionId.getOrDefault(id, List.of()).stream())
                .toList();

        var variablesMap = variableRuntimeService.toMap(variablesInScope);
        return expressionEvaluator.evaluateBoolean(condition, variablesMap);
    }

}
