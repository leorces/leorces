package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateVariablesCommand;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import com.leorces.persistence.VariablePersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetVariablesCommandHandler implements CommandHandler<SetVariablesCommand> {

    private final VariablePersistence variablePersistence;
    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final VariablesMapper variablesMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(SetVariablesCommand command) {
        var executionId = command.executionId();
        var process = command.process();
        var variables = command.variables();

        if (variables.isEmpty()) {
            return;
        }

        if (process != null) {
            setProcessVariables(process, variables);
            return;
        }

        getProcessById(executionId)
                .ifPresentOrElse(
                        proc -> setProcessVariables(proc, variables),
                        () -> getActivityById(executionId)
                                .ifPresent(activity -> updateActivityVariables(activity, variables, command.local()))
                );
    }

    @Override
    public Class<SetVariablesCommand> getCommandType() {
        return SetVariablesCommand.class;
    }

    private void setProcessVariables(Process process, Map<String, Object> input) {
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

        Function<Variable, Variable> newVariableMapper = localOnly
                ? variable -> variablesMapper.map(activity, variable)
                : variable -> variablesMapper.map(activity.process(), variable);

        var updated = mergeAndPersist(input, existing, newVariableMapper);
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
        dispatcher.dispatch(CorrelateVariablesCommand.of(process, variables));
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
