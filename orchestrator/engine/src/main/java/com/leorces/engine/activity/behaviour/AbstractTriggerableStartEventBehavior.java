package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.start.StartEventActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;

import java.util.List;
import java.util.Map;

public abstract class AbstractTriggerableStartEventBehavior
        extends AbstractActivityBehavior
        implements TriggerableActivityBehaviour {

    protected AbstractTriggerableStartEventBehavior(ActivityPersistence activityPersistence,
                                                    CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        var eventSubprocess = getEventSubprocess(process, definition);
        dispatcher.dispatchAsync(RunActivityCommand.of(process, eventSubprocess));
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedActivity = activityPersistence.complete(activity);
        var definition = (StartEventActivityDefinition) completedActivity.definition();

        if (definition.isInterrupting()) {
            interrupt(completedActivity);
        }

        postComplete(completedActivity, variables);
    }

    private void interrupt(ActivityExecution activity) {
        var eventSubprocess = getEventSubprocess(activity.process(), activity.definition());

        if (eventSubprocess.parentId() == null) {
            var activitiesToTerminate = getActivitiesToTerminate(activity.process());
            dispatcher.dispatch(TerminateAllActivitiesCommand.of(activitiesToTerminate));
            return;
        }

        var activitiesToTerminate = getActivitiesToTerminate(activity.process(), eventSubprocess.parentId());
        dispatcher.dispatch(TerminateAllActivitiesCommand.of(activitiesToTerminate));
    }


    private ActivityDefinition getEventSubprocess(Process process, ActivityDefinition definition) {
        return process.definition().getActivityById(definition.parentId())
                .orElseThrow(ActivityNotFoundException::eventSubprocessNotFound);
    }

    private List<ActivityExecution> getActivitiesToTerminate(Process process, String definitionId) {
        return activityPersistence.findActive(process.id(), getParentSubprocessActivityIds(process, definitionId)).stream()
                .filter(activity -> !activity.isAsync())
                .toList();
    }

    private List<ActivityExecution> getActivitiesToTerminate(Process process) {
        return activityPersistence.findActive(process.id()).stream()
                .filter(activity -> !activity.isAsync())
                .toList();
    }

    private List<String> getParentSubprocessActivityIds(Process process, String definitionId) {
        return process.definition().findChildActivities(definitionId).stream()
                .map(ActivityDefinition::id)
                .toList();
    }

}
