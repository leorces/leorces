package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.correlation.command.CorrelateMessageCommand;
import com.leorces.model.definition.activity.ExternalTaskDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.util.Map;

public abstract class AbstractThrowMessageBehavior extends AbstractExternalTaskBehavior {

    protected AbstractThrowMessageBehavior(ActivityPersistence activityPersistence,
                                           CommandDispatcher dispatcher,
                                           EngineProperties engineProperties) {
        super(activityPersistence, dispatcher, engineProperties);
    }

    @Override
    public void run(ActivityExecution messageThrowActivity) {
        var definition = (ExternalTaskDefinition) messageThrowActivity.definition();

        if (definition.topic() != null && !definition.topic().isBlank()) {
            super.run(messageThrowActivity);
        } else {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(messageThrowActivity));
        }
    }

    @Override
    public void complete(ActivityExecution messageThrowActivity, Map<String, Object> variables) {
        var completedMessageThrowEvent = activityPersistence.complete(messageThrowActivity);
        correlateMessage(completedMessageThrowEvent);
        postComplete(completedMessageThrowEvent, variables);
    }

    private void correlateMessage(ActivityExecution messageThrowActivity) {
        var definition = (MessageActivityDefinition) messageThrowActivity.definition();
        var businessKey = messageThrowActivity.process().businessKey();
        var message = definition.messageReference();

        dispatcher.dispatchAsync(CorrelateMessageCommand.of(message, businessKey));
    }

}
