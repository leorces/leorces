package com.leorces.engine.activity.behaviour.task;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ReceiveTaskBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    protected ReceiveTaskBehavior(ActivityPersistence activityPersistence,
                                  CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        activityPersistence.findByDefinitionId(process.id(), definition.id())
                .ifPresent(activity -> dispatcher.dispatchAsync(CompleteActivityCommand.of(activity)));
    }

    @Override
    public void run(ActivityExecution activity) {
        activityPersistence.run(activity);
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        completeEventBasedGatewayActivities(result);
        return result;
    }

    @Override
    public ActivityExecution terminate(ActivityExecution activity) {
        var result = activityPersistence.terminate(activity);
        completeEventBasedGatewayActivities(result);
        return result;
    }

    @Override
    public ActivityType type() {
        return ActivityType.RECEIVE_TASK;
    }

}
