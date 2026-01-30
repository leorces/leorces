package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.variables.command.EvaluateVariablesCommand;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EvaluateVariablesCommandHandler implements ResultCommandHandler<EvaluateVariablesCommand, List<Variable>> {

    private final ExpressionEvaluator expressionEvaluator;
    private final VariablesMapper variablesMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public List<Variable> execute(EvaluateVariablesCommand command) {
        var variables = command.variables();
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }
        return hasExpression(variables)
                ? evaluateWithExpressions(variables, command.activity())
                : variablesMapper.map(variables);
    }

    @Override
    public Class<EvaluateVariablesCommand> getCommandType() {
        return EvaluateVariablesCommand.class;
    }

    private List<Variable> evaluateWithExpressions(Map<String, Object> variablesMap, ActivityExecution activity) {
        var variables = dispatcher.execute(GetScopedVariablesCommand.of(activity));
        var evaluated = expressionEvaluator.evaluate(variablesMap, variables);
        return variablesMapper.map(evaluated);
    }

    private boolean hasExpression(Map<String, Object> variables) {
        return variables.values().stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .anyMatch(expressionEvaluator::isExpression);
    }

}
