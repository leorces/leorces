package com.leorces.engine.activity.behaviour.event.intermediate;

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

import java.util.Map;

@Component
public class MessageIntermediateCatchEventBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    protected MessageIntermediateCatchEventBehavior(ActivityPersistence activityPersistence,
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
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedActivity = activityPersistence.complete(activity);
        completeEventBasedGatewayActivities(completedActivity);
        postComplete(completedActivity, variables);
    }

    @Override
    public void terminate(ActivityExecution activity, boolean withInterruption) {
        var terminatedActivity = activityPersistence.terminate(activity);
        completeEventBasedGatewayActivities(terminatedActivity);
        postTerminate(terminatedActivity, withInterruption);
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT;
    }

}
