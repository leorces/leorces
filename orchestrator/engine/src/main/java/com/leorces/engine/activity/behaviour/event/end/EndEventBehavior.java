package com.leorces.engine.activity.behaviour.event.end;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EndEventBehavior extends AbstractActivityBehavior {

    protected EndEventBehavior(ActivityPersistence activityPersistence,
                               CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedActivity = activityPersistence.complete(activity);
        dispatcher.dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(completedActivity));
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.END_EVENT;
    }

}
