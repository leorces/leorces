package com.leorces.engine.activity.behaviour.event.intermediate;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.correlation.command.CorrelateEscalationCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EscalationIntermediateThrowEventBehavior extends AbstractActivityBehavior {

    protected EscalationIntermediateThrowEventBehavior(ActivityPersistence activityPersistence,
                                                       CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedEscalationEndEvent = activityPersistence.complete(activity);
        dispatcher.dispatch(CorrelateEscalationCommand.of(completedEscalationEndEvent));
        postComplete(completedEscalationEndEvent, variables);
    }

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_INTERMEDIATE_THROW_EVENT;
    }

}
