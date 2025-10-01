package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;

import java.util.Optional;

public abstract class AbstractBoundaryEventBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    protected AbstractBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                            CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        dispatcher.dispatchAsync(RunActivityCommand.of(process, definition));
    }

    @Override
    public void run(ActivityExecution activity) {
        var boundaryEvent = (BoundaryEventDefinition) activity.definition();
        var attachedActivity = activityPersistence.findByDefinitionId(activity.processId(), boundaryEvent.attachedToRef());

        if (!shouldRun(attachedActivity)) {
            return;
        }

        if (boundaryEvent.cancelActivity()) {
            dispatcher.dispatch(TerminateActivityCommand.of(attachedActivity.get(), true));
        }

        dispatcher.dispatch(CompleteActivityCommand.of(activity));
    }

    protected boolean shouldRun(Optional<ActivityExecution> attachedActivity) {
        return attachedActivity.isPresent() && attachedActivity.get().state() == ActivityState.ACTIVE;
    }

}
