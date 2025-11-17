package com.leorces.engine.service.process;

import com.leorces.engine.exception.process.ProcessDefinitionNotFoundException;
import com.leorces.engine.service.activity.CallActivityService;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessFactory {

    private final DefinitionPersistence definitionPersistence;
    private final VariablesService variablesService;
    private final CallActivityService callActivityService;

    public Process createByDefinitionId(String definitionId,
                                        String businessKey,
                                        Map<String, Object> variables) {
        var definition = definitionPersistence.findById(definitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(definitionId));

        return Process.builder()
                .businessKey(businessKey)
                .variables(variablesService.toList(variables))
                .definition(definition)
                .build();
    }

    public Process createByDefinitionKey(String definitionKey,
                                         String businessKey,
                                         Map<String, Object> variables) {
        var definition = getDefinitionByKey(definitionKey);
        return Process.builder()
                .businessKey(businessKey)
                .variables(variablesService.toList(variables))
                .definition(definition)
                .build();
    }

    public Process createByCallActivity(ActivityExecution activity) {
        var callActivity = (CallActivity) activity.definition();
        var definition = getDefinition(callActivity.calledElement(), callActivity.calledElementVersion());
        var variables = callActivityService.getInputMappings(activity);
        var rootProcessId = activity.process().rootProcessId() == null
                ? activity.process().id()
                : activity.process().rootProcessId();


        return Process.builder()
                .id(activity.id())
                .parentId(activity.process().id())
                .rootProcessId(rootProcessId)
                .businessKey(activity.process().businessKey())
                .definition(definition)
                .variables(variablesService.toList(variables))
                .build();
    }

    private ProcessDefinition getDefinition(String definitionKey, Integer version) {
        if (version == null) {
            return definitionPersistence.findLatestByKey(definitionKey)
                    .orElseThrow(() -> ProcessDefinitionNotFoundException.byKey(definitionKey));
        }
        return definitionPersistence.findByKeyAndVersion(definitionKey, version)
                .orElseThrow(() -> ProcessDefinitionNotFoundException.byKeyAndVersion(definitionKey, version));
    }

    private ProcessDefinition getDefinitionByKey(String definitionKey) {
        return definitionPersistence.findLatestByKey(definitionKey)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(definitionKey));
    }

}
