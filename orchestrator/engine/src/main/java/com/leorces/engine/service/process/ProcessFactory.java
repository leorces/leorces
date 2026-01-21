package com.leorces.engine.service.process;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.service.activity.CallActivityService;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessFactory {

    private final DefinitionPersistence definitionPersistence;
    private final VariablesService variablesService;
    private final ExpressionEvaluator expressionEvaluator;
    private final CallActivityService callActivityService;

    public Process createByDefinitionId(String definitionId,
                                        String businessKey,
                                        Map<String, Object> variables) {
        var definition = findDefinitionById(definitionId);
        return buildProcess(definition, businessKey, variables);
    }

    public Process createByDefinitionKey(String definitionKey,
                                         String businessKey,
                                         Map<String, Object> variables) {
        var definition = findLatestDefinition(definitionKey);
        return buildProcess(definition, businessKey, variables);
    }

    public Process createByCallActivity(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();
        var scopedVariables = resolveScopedVariables(activity, callActivity);
        var inputMappings = callActivityService.getInputMappings(activity, scopedVariables);
        var definition = resolveDefinition(callActivity, scopedVariables);

        return buildProcessFromActivity(activity, definition, inputMappings);
    }

    private Map<String, Object> resolveScopedVariables(ActivityExecution activity, CallActivity callActivity) {
        return shouldFetchScopedVariables(callActivity)
                ? variablesService.getScopedVariables(activity)
                : Map.of();
    }

    private ProcessDefinition resolveDefinition(CallActivity callActivity, Map<String, Object> variables) {
        var calledElement = getCalledElement(callActivity, variables)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Called element is null"));
        return getDefinition(calledElement, callActivity.calledElementVersion());
    }

    private Process buildProcessFromActivity(ActivityExecution activity,
                                             ProcessDefinition definition,
                                             Map<String, Object> inputMappings) {
        var parentProcess = activity.process();
        return Process.builder()
                .id(activity.id())
                .parentId(parentProcess.id())
                .rootProcessId(resolveRootProcessId(parentProcess))
                .businessKey(parentProcess.businessKey())
                .definition(definition)
                .variables(variablesService.toList(inputMappings))
                .suspended(parentProcess.suspended())
                .build();
    }

    private Process buildProcess(ProcessDefinition definition, String businessKey, Map<String, Object> variables) {
        return Process.builder()
                .businessKey(businessKey)
                .variables(variablesService.toList(variables))
                .definition(definition)
                .build();
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

    private ProcessDefinition findDefinitionById(String definitionId) {
        return definitionPersistence.findById(definitionId)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Process definition not found by id: %s".formatted(definitionId)));
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
