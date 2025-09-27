package com.leorces.engine.variables;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.correlation.CorrelationEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import com.leorces.persistence.VariablePersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class VariablesUpdateService {

    private final VariablesMapper variablesMapper;
    private final VariablePersistence variablePersistence;
    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    public void setVariables(String executionId, Map<String, Object> variables) {
        getProcessById(executionId)
                .ifPresentOrElse(
                        process -> setProcessVariables(process, variables),
                        () -> getActivityById(executionId)
                                .ifPresent(activity -> updateActivityVariables(activity, variables, false))
                );
    }

    public void setVariablesLocal(String executionId, Map<String, Object> variables) {
        getProcessById(executionId)
                .ifPresentOrElse(
                        process -> setProcessVariables(process, variables),
                        () -> getActivityById(executionId)
                                .ifPresent(activity -> updateActivityVariables(activity, variables, true))
                );
    }

    public void setProcessVariables(Process process, Map<String, Object> input) {
        var existing = toMapByKey(process.variables());
        var updated = mergeAndPersist(input, existing, variable -> variablesMapper.map(process, variable));
        correlate(process, updated);
    }

    private void updateActivityVariables(ActivityExecution activity, Map<String, Object> input, boolean localOnly) {
        var existing = activity.variables().stream()
                .filter(variable -> localOnly
                        ? Objects.equals(variable.executionId(), activity.id())
                        : activity.scope().contains(variable.executionDefinitionId()))
                .collect(Collectors.toMap(Variable::varKey, Function.identity(), (a, b) -> a));

        var updated = mergeAndPersist(input, existing, v -> variablesMapper.map(activity.process(), v));
        correlate(activity.process(), updated);
    }

    private List<Variable> mergeAndPersist(Map<String, Object> input,
                                           Map<String, Variable> existingByKey,
                                           Function<Variable, Variable> newVariableMapper) {
        var incoming = variablesMapper.map(input);

        var updated = incoming.stream()
                .filter(v -> existingByKey.containsKey(v.varKey()))
                .map(v -> updateExisting(existingByKey.get(v.varKey()), v))
                .toList();

        var updatedKeys = updated.stream().map(Variable::varKey).collect(Collectors.toSet());
        var newOnes = incoming.stream()
                .filter(v -> !updatedKeys.contains(v.varKey()) && !existingByKey.containsKey(v.varKey()))
                .map(newVariableMapper)
                .toList();

        var merged = new ArrayList<Variable>(updated.size() + newOnes.size());
        merged.addAll(updated);
        merged.addAll(newOnes);

        return variablePersistence.update(merged);
    }

    private Variable updateExisting(Variable oldVar, Variable newVar) {
        return oldVar.toBuilder()
                .varValue(newVar.varValue())
                .type(newVar.type())
                .build();
    }

    private void correlate(Process process, List<Variable> variables) {
        eventBus.publish(CorrelationEvent.variables(process, variables));
    }

    private Map<String, Variable> toMapByKey(Collection<Variable> variables) {
        return variables.stream()
                .collect(Collectors.toMap(Variable::varKey, Function.identity(), (a, b) -> a));
    }

    private Optional<Process> getProcessById(String processId) {
        return processPersistence.findById(processId);
    }

    private Optional<ActivityExecution> getActivityById(String activityId) {
        return activityPersistence.findById(activityId);
    }

}
