package com.leorces.engine.activity;

import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ActivityFactory {

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
                .orElseThrow(() -> ActivityNotFoundException.activityNotFoundById(activityId));
    }

    public ActivityExecution getNewByDefinitionId(String definitionId, String processId) {
        var process = processPersistence.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        var definition = process.definition().getActivityById(definitionId)
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(definitionId, processId));
        return createActivity(definition, process);
    }

    public ActivityExecution getByDefinitionId(String definitionId, String processId) {
        return activityPersistence.findByDefinitionId(processId, definitionId)
                .orElseThrow(() -> ActivityNotFoundException.activityNotFoundByProcessAndDefinition(processId, definitionId));
    }

}
