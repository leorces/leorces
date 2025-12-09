package com.leorces.engine.activity.behaviour.event.end;

import com.leorces.engine.activity.behaviour.AbstractExternalTaskBehavior;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.correlation.command.CorrelateMessageCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.event.end.MessageEndEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessageEndEventBehavior extends AbstractExternalTaskBehavior {

    protected MessageEndEventBehavior(ActivityPersistence activityPersistence,
                                      CommandDispatcher dispatcher,
                                      EngineProperties engineProperties) {
        super(activityPersistence, dispatcher, engineProperties);
    }

    @Override
    public void run(ActivityExecution messageEndEvent) {
        var definition = (MessageEndEvent) messageEndEvent.definition();

        if (definition.topic() != null && !definition.topic().isBlank()) {
            super.run(messageEndEvent);
        } else {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(messageEndEvent));
        }
    }

    @Override
    public void complete(ActivityExecution messageEndEvent, Map<String, Object> variables) {
        var completedMessageEndEvent = activityPersistence.complete(messageEndEvent);
        correlateMessage(completedMessageEndEvent);
        postComplete(completedMessageEndEvent, variables);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution endEvent) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_END_EVENT;
    }

    private void correlateMessage(ActivityExecution messageEndEvent) {
        var definition = (MessageEndEvent) messageEndEvent.definition();
        var businessKey = messageEndEvent.process().businessKey();
        var message = definition.messageReference();

        dispatcher.dispatch(CorrelateMessageCommand.of(message, businessKey));
    }

}
