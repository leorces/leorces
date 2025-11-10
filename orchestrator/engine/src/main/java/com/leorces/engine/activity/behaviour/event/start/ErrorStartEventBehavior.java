package com.leorces.engine.activity.behaviour.event.start;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ErrorStartEventBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    protected ErrorStartEventBehavior(ActivityPersistence activityPersistence,
                                      CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        var eventSubprocess = getEventSubprocess(process, definition);
        dispatcher.dispatchAsync(RunActivityCommand.of(process, eventSubprocess));
    }

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_START_EVENT;
    }

    private ActivityDefinition getEventSubprocess(Process process, ActivityDefinition definition) {
        return process.definition().getActivityById(definition.parentId())
                .orElseThrow(ActivityNotFoundException::eventSubprocessNotFound);
    }

}
