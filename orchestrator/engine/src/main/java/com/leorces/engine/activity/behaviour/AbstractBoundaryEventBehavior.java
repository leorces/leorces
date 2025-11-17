package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;

import java.util.Optional;

public abstract class AbstractBoundaryEventBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    protected AbstractBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                            CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void trigger(Process process, ActivityDefinition boundaryEventDefinition) {
        dispatcher.dispatchAsync(RunActivityCommand.of(process, boundaryEventDefinition));
    }

    @Override
    public void run(ActivityExecution boundaryEvent) {
        var boundaryEventDefinition = (BoundaryEventDefinition) boundaryEvent.definition();
        var attachedActivity = findAttachedActivity(boundaryEvent, boundaryEventDefinition);

        if (attachedActivity.isEmpty()) {
            return;
        }

        var attachedExecution = attachedActivity.get();
        if (!canRun(attachedExecution)) {
            return;
        }

        if (boundaryEventDefinition.cancelActivity()) {
            terminateAttachedActivity(attachedExecution);
        }

        completeBoundaryEvent(boundaryEvent);
    }

    protected boolean canRun(ActivityExecution attachedActivity) {
        return true;
    }

    private Optional<ActivityExecution> findAttachedActivity(ActivityExecution boundaryExecution,
                                                             BoundaryEventDefinition boundaryEvent) {
        return activityPersistence.findByDefinitionId(boundaryExecution.processId(), boundaryEvent.attachedToRef());
    }

    private void terminateAttachedActivity(ActivityExecution attachedExecution) {
        dispatcher.dispatch(TerminateActivityCommand.of(attachedExecution, true));
    }

    private void completeBoundaryEvent(ActivityExecution activity) {
        dispatcher.dispatch(CompleteActivityCommand.of(activity));
    }

}
