package com.leorces.engine.correlation.handler;

import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateVariablesCommand;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ConditionalActivityDefinition;
import com.leorces.model.runtime.variable.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelateVariablesCommandHandler implements CommandHandler<CorrelateVariablesCommand> {

    private final VariablesService variablesService;
    private final ExpressionEvaluator expressionEvaluator;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CorrelateVariablesCommand command) {
        var process = command.process();
        var variables = command.variables();

        log.debug("Correlate variables with processId: {} and variables: {}", process.id(), variables);
        if (variables.isEmpty()) return;

        var processDefinition = process.definition();
        var variablesByExecutionId = variables.stream()
                .collect(Collectors.groupingBy(Variable::executionDefinitionId));

        processDefinition.activities().stream()
                .filter(ConditionalActivityDefinition.class::isInstance)
                .map(ConditionalActivityDefinition.class::cast)
                .filter(definition -> isConditionMet(definition, processDefinition, variablesByExecutionId))
                .map(definition -> TriggerActivityCommand.of(process, definition))
                .forEach(dispatcher::dispatch);

        log.debug("Correlate variables with processId: {} and variables: {} successfully", process.id(), variables);
    }

    @Override
    public Class<CorrelateVariablesCommand> getCommandType() {
        return CorrelateVariablesCommand.class;
    }

    private boolean isConditionMet(ConditionalActivityDefinition activityDefinition,
                                   ProcessDefinition processDefinition,
                                   Map<String, List<Variable>> variablesByExecutionId) {
        var condition = activityDefinition.condition();
        var scope = processDefinition.scope(activityDefinition.id());

        var variablesInScope = scope.stream()
                .flatMap(id -> variablesByExecutionId.getOrDefault(id, List.of()).stream())
                .toList();

        var variablesMap = variablesService.toMap(variablesInScope);
        return expressionEvaluator.evaluateBoolean(condition, variablesMap);
    }

}
