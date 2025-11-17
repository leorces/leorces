package com.leorces.engine.activity.behaviour.event.end;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TerminateEndEventBehavior extends AbstractActivityBehavior {

    protected TerminateEndEventBehavior(ActivityPersistence activityPersistence,
                                        CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void complete(ActivityExecution terminateEndEvent, Map<String, Object> variables) {
        var completedTerminateEndEvent = activityPersistence.complete(terminateEndEvent);
        var processId = completedTerminateEndEvent.processId();

        if (!terminateEndEvent.hasParent()) {
            terminateProcess(processId);
        } else {
            terminateParentActivity(completedTerminateEndEvent);
        }
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.TERMINATE_END_EVENT;
    }

    private void terminateParentActivity(ActivityExecution terminateEndEvent) {
        var parent = getParentActivity(terminateEndEvent);

        if (parent.type().isEventSubprocess()) {
            terminateEventSubprocess(parent);
        } else {
            dispatcher.dispatch(TerminateActivityCommand.of(parent, false));
        }
    }

    private void terminateEventSubprocess(ActivityExecution eventSubprocess) {
        dispatcher.dispatch(TerminateActivityCommand.of(eventSubprocess, true));

        var process = eventSubprocess.process();

        if (!eventSubprocess.hasParent()) {
            terminateProcess(process.id());
            return;
        }

        dispatcher.dispatch(TerminateActivityCommand.of(
                process.id(),
                eventSubprocess.parentDefinitionId(),
                false
        ));
    }

    private void terminateProcess(String processId) {
        dispatcher.dispatch(TerminateProcessCommand.of(processId));
    }

    private ActivityExecution getParentActivity(ActivityExecution terminateEndEvent) {
        var processId = terminateEndEvent.processId();
        var parentDefinitionId = terminateEndEvent.parentDefinitionId();

        return activityPersistence.findByDefinitionId(processId, parentDefinitionId)
                .orElseThrow(() ->
                        ActivityNotFoundException.activityDefinitionNotFound(parentDefinitionId, processId)
                );
    }

}
