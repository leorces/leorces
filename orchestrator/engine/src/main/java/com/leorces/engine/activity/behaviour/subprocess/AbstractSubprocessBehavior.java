package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.util.List;

public abstract class AbstractSubprocessBehavior extends AbstractActivityBehavior {

    protected AbstractSubprocessBehavior(ActivityPersistence activityPersistence,
                                         CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution subprocess) {
        var newSubprocess = activityPersistence.run(subprocess);
        dispatcher.dispatchAsync(RunActivityCommand.of(newSubprocess.process(), getStartEvent(newSubprocess)));
    }

    @Override
    public void terminate(ActivityExecution subprocess, boolean withInterruption) {
        terminateChildActivities(subprocess);
        var terminatedSubprocess = activityPersistence.terminate(subprocess);
        postTerminate(terminatedSubprocess, withInterruption);
    }

    protected boolean isAllChildActivitiesCompleted(ActivityExecution subprocess) {
        return activityPersistence.isAllCompleted(
                subprocess.processId(),
                getChildActivityIds(subprocess)
        );
    }

    protected List<String> getChildActivityIds(ActivityExecution subprocess) {
        return subprocess.childActivities().stream()
                .map(ActivityDefinition::id)
                .toList();
    }

    protected ActivityDefinition getStartEvent(ActivityExecution subprocess) {
        return subprocess.processDefinition().activities().stream()
                .filter(activity -> subprocess.definitionId().equals(activity.parentId()))
                .filter(activity -> activity.type().isStartEvent())
                .findFirst()
                .orElseThrow(() -> ExecutionException.of("Can't start subprocess", "Start event not found for subprocess: %s".formatted(subprocess.definitionId()), subprocess));
    }

    private void terminateChildActivities(ActivityExecution subprocess) {
        var childActivityIds = getChildActivityIds(subprocess);
        var childActivities = activityPersistence.findActive(subprocess.processId(), childActivityIds);
        dispatcher.dispatch(TerminateAllActivitiesCommand.of(childActivities));
    }

}
