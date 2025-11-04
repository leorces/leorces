package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityCompletionResult;
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
    public ActivityCompletionResult complete(ActivityExecution activity) {
        var completedActivity = activityPersistence.complete(activity);
        completeEventBasedGatewayActivities(completedActivity);
        return ActivityCompletionResult.completed(completedActivity, getNextActivities(completedActivity));
    }

    @Override
    public ActivityCompletionResult terminate(ActivityExecution activity) {
        var terminatedActivity = activityPersistence.terminate(activity);
        completeEventBasedGatewayActivities(terminatedActivity);
        return ActivityCompletionResult.completed(terminatedActivity, getNextActivities(terminatedActivity));
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT;
    }

}
