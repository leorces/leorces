package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractTriggerableCatchBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    protected AbstractTriggerableCatchBehavior(ActivityPersistence activityPersistence,
                                               CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void trigger(Process process, ActivityDefinition intermediateCatchEventDefinition) {
        activityPersistence.findByDefinitionId(process.id(), intermediateCatchEventDefinition.id())
                .map(CompleteActivityCommand::of)
                .ifPresent(dispatcher::dispatchAsync);
    }

    @Override
    public void complete(ActivityExecution intermediateCatchEvent, Map<String, Object> variables) {
        var completedIntermediateCatchEvent = activityPersistence.complete(intermediateCatchEvent);
        completeEventBasedGatewayActivities(completedIntermediateCatchEvent);
        postComplete(completedIntermediateCatchEvent, variables);
    }

    @Override
    public void terminate(ActivityExecution intermediateCatchEvent, boolean withInterruption) {
        var terminatedIntermediateCatchEvent = activityPersistence.terminate(intermediateCatchEvent);
        completeEventBasedGatewayActivities(terminatedIntermediateCatchEvent);
        postTerminate(terminatedIntermediateCatchEvent, withInterruption);
    }

    private void completeEventBasedGatewayActivities(ActivityExecution intermediateCatchEvent) {
        findEventBasedGateway(intermediateCatchEvent)
                .ifPresent(eventBasedGateway ->
                        activityPersistence.deleteAllActive(intermediateCatchEvent.processId(), eventBasedGateway.outgoing())
                );
    }

    private Optional<ActivityDefinition> findEventBasedGateway(ActivityExecution activity) {
        return activity.previousActivities().stream()
                .filter(definition -> ActivityType.EVENT_BASED_GATEWAY.equals(definition.type()))
                .findFirst();
    }

}
