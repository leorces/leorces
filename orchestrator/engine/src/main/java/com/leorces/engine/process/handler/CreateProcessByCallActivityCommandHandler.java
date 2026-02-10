package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.activity.command.GetCallActivityMappingsCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.process.command.CreateProcessByCallActivityCommand;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateProcessByCallActivityCommandHandler implements ResultCommandHandler<CreateProcessByCallActivityCommand, Process> {

    private final VariablesMapper variablesMapper;
    private final DefinitionPersistence definitionPersistence;
    private final ExpressionEvaluator expressionEvaluator;
    private final CommandDispatcher dispatcher;

    @Override
    public Process execute(CreateProcessByCallActivityCommand command) {
        Objects.requireNonNull(command);
        var activity = command.callActivity();
        var callActivity = (CallActivity) activity.definition();
        var scopedVariables = resolveScopedVariables(activity, callActivity);
        var inputMappings = resolveInputMappings(activity, scopedVariables);
        var definition = resolveDefinition(callActivity, scopedVariables);
        return buildProcess(activity, definition, inputMappings, command.additionalVariables());
    }

    private Process buildProcess(ActivityExecution activity,
                                 ProcessDefinition definition,
                                 Map<String, Object> inputMappings,
                                 Map<String, Object> additionalVariables) {
        var parentProcess = activity.process();
        return Process.builder()
                .id(activity.id())
                .parentId(parentProcess.id())
                .rootProcessId(resolveRootProcessId(parentProcess))
                .businessKey(parentProcess.businessKey())
                .definition(definition)
                .variables(buildVariables(inputMappings, additionalVariables))
                .suspended(parentProcess.suspended())
                .build();
    }

    private Map<String, Object> resolveInputMappings(ActivityExecution activity, Map<String, Object> scopedVariables) {
        var mappings = dispatcher.execute(GetCallActivityMappingsCommand.input(activity, scopedVariables));
        return mappings != null ? mappings : Map.of();
    }

    @Override
    public Class<CreateProcessByCallActivityCommand> getCommandType() {
        return CreateProcessByCallActivityCommand.class;
    }

    private Map<String, Object> resolveScopedVariables(ActivityExecution activity, CallActivity callActivity) {
        return shouldFetchScopedVariables(callActivity)
                ? activity.getScopedVariables(() -> dispatcher.execute(GetScopedVariablesCommand.of(activity)))
                : Map.of();
    }

    private ProcessDefinition resolveDefinition(CallActivity callActivity, Map<String, Object> variables) {
        return getCalledElement(callActivity, variables)
                .map(calledElement -> getDefinition(calledElement, callActivity.calledElementVersion()))
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Called element is null"));
    }

    private List<Variable> buildVariables(Map<String, Object> variables,
                                          Map<String, Object> additionalVariables) {
        var allVariables = new HashMap<>(variables);
        if (additionalVariables != null) {
            allVariables.putAll(additionalVariables);
        }
        return mapVariables(allVariables);
    }

    private List<Variable> mapVariables(Map<String, Object> variables) {
        return variablesMapper.map(variables);
    }

    private String resolveRootProcessId(Process parentProcess) {
        return parentProcess.rootProcessId() == null
                ? parentProcess.id()
                : parentProcess.rootProcessId();
    }

    private ProcessDefinition getDefinition(String definitionKey, Integer version) {
        return version == null
                ? findLatestDefinition(definitionKey)
                : findDefinitionByVersion(definitionKey, version);
    }

    private ProcessDefinition findLatestDefinition(String definitionKey) {
        return definitionPersistence.findLatestByKey(definitionKey)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Latest process definition not found for key: %s".formatted(definitionKey)));
    }

    private ProcessDefinition findDefinitionByVersion(String definitionKey, Integer version) {
        return definitionPersistence.findByKeyAndVersion(definitionKey, version)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Process definition not found by key: %s and version: %s".formatted(definitionKey, version)));
    }

    private boolean shouldFetchScopedVariables(CallActivity callActivity) {
        var isExpression = callActivity.calledElement() != null && expressionEvaluator.isExpression(callActivity.calledElement());
        var hasMappings = callActivity.inputMappings() != null && !callActivity.inputMappings().isEmpty();
        return isExpression || hasMappings;
    }

    private Optional<String> getCalledElement(CallActivity callActivity, Map<String, Object> variables) {
        var calledElement = callActivity.calledElement();
        if (calledElement == null) {
            return Optional.empty();
        }
        if (expressionEvaluator.isExpression(calledElement)) {
            return Optional.ofNullable(expressionEvaluator.evaluateString(calledElement, variables));
        }
        return Optional.of(calledElement);
    }

}
