package com.leorces.engine.activity.behaviour.task;

import com.leorces.common.utils.RelativeTimeParser;
import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.RetryActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ExternalTaskBehavior extends AbstractActivityBehavior {

    private static final int DEFAULT_TIMEOUT_IN_HOURS = 1;

    protected ExternalTaskBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution activity) {
        activityPersistence.schedule(setTimeout(activity));
    }

    @Override
    public boolean fail(ActivityExecution activity) {
        var externalTaskDefinition = (ExternalTask) activity.definition();

        if (activity.retries() < externalTaskDefinition.retries()) {
            dispatcher.dispatchAsync(RetryActivityCommand.of(activity));
            return false;
        }

        activityPersistence.fail(activity);
        return true;
    }

    @Override
    public void retry(ActivityExecution activity) {
        activityPersistence.schedule(incrementRetries(activity));
    }

    @Override
    public ActivityType type() {
        return ActivityType.EXTERNAL_TASK;
    }

    private ActivityExecution setTimeout(ActivityExecution activity) {
        var definition = (ExternalTask) activity.definition();
        if (definition.timeout() != null) {
            return activity.toBuilder()
                    .timeout(RelativeTimeParser.parseRelative(definition.timeout()))
                    .build();
        }

        return activity.toBuilder()
                .timeout(LocalDateTime.now().plusHours(DEFAULT_TIMEOUT_IN_HOURS))
                .build();
    }

    private ActivityExecution incrementRetries(ActivityExecution activity) {
        return activity.toBuilder()
                .retries(activity.retries() + 1)
                .build();
    }

}
