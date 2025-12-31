package com.leorces.engine.service.activity;

import com.leorces.api.exception.ExecutionException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityFactory {

    private final ActivityPersistence activityPersistence;
    private final ProcessPersistence processPersistence;

    public ActivityExecution createActivity(ActivityDefinition definition, Process process) {
        return ActivityExecution.builder()
                .definitionId(definition.id())
                .process(process)
                .build();
    }

    public ActivityExecution getById(String activityId) {
        return activityPersistence.findById(activityId)
                .orElseThrow(() -> ExecutionException.of("Activity not found", "Activity with id: %s not found".formatted(activityId)));
    }

    public ActivityExecution getNewByDefinitionId(String definitionId, String processId) {
        var process = processPersistence.findById(processId)
                .orElseThrow(() -> ExecutionException.of("Process not found", "Process with id: %s not found".formatted(processId)));
        var definition = process.definition().getActivityById(definitionId)
                .orElseThrow(() -> ExecutionException.of("Activity definition not found", "Activity definition not found for definitionId: %s in process: %s".formatted(definitionId, processId), process));
        return createActivity(definition, process);
    }

    public ActivityExecution getByDefinitionId(String definitionId, String processId) {
        return activityPersistence.findByDefinitionId(processId, definitionId)
                .orElseThrow(() -> ExecutionException.of("Activity not found", "Activity not found for process: %s and definition: %s".formatted(processId, definitionId)));
    }

}
