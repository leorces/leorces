package com.leorces.engine.variables;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.VariablePersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
class VariableEvaluationService {

    private final VariablesMapper variablesMapper;
    private final VariablePersistence variablePersistence;
    private final ExpressionEvaluator expressionEvaluator;

    public List<Variable> evaluate(ActivityExecution activity, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }
        return hasExpression(variables)
                ? evaluateWithExpressions(variables, activity)
                : variablesMapper.map(variables);
    }

    private List<Variable> evaluateWithExpressions(Map<String, Object> variablesMap, ActivityExecution activity) {
        var variables = getVariablesAsMap(activity);
        var evaluated = expressionEvaluator.evaluate(variablesMap, variables);
        return variablesMapper.map(evaluated);
    }

    public Map<String, Object> getVariablesAsMap(ActivityExecution activity) {
        var scope = activity.scope();
        var variables = variablePersistence.findInScope(activity.processId(), scope);
        return variablesMapper.toMap(variables, scope);
    }

    private boolean hasExpression(Map<String, Object> variables) {
        return variables.values().stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(expressionEvaluator::isExpression)
                .toList()
                .contains(true);
    }

}
