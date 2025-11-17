package com.leorces.engine.service.variable;

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
public class VariablesService {

    private final ExpressionEvaluator expressionEvaluator;
    private final VariablesMapper variablesMapper;
    private final VariablePersistence variablePersistence;

    public Map<String, Object> getScopedVariables(ActivityExecution activity) {
        var scope = activity.scope();
        var variables = variablePersistence.findInScope(activity.processId(), scope);
        return variablesMapper.toMap(variables, scope);
    }

    public Map<String, Object> getProcessVariables(String processId) {
        var variables = variablePersistence.findInProcess(processId);
        return variablesMapper.toMap(variables);
    }

    public List<Variable> toList(Map<String, Object> variables) {
        return variablesMapper.map(variables);
    }

    public Map<String, Object> toMap(List<Variable> variables) {
        return variablesMapper.toMap(variables);
    }

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

    private Map<String, Object> getVariablesAsMap(ActivityExecution activity) {
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
