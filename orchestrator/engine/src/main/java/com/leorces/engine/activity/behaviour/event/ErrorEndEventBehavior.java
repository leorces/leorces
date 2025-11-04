package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityCompletionResult;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.correlation.command.CorrelateErrorCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ErrorEndEventBehavior extends AbstractActivityBehavior {

    protected ErrorEndEventBehavior(ActivityPersistence activityPersistence,
                                    CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityCompletionResult complete(ActivityExecution activity) {
        var completedActivity = activityPersistence.complete(activity);
        dispatcher.dispatch(CorrelateErrorCommand.of(completedActivity));
        return ActivityCompletionResult.completed(completedActivity, getNextActivities(completedActivity));
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_END_EVENT;
    }

}
