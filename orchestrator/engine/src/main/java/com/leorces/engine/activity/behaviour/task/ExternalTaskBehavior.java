package com.leorces.engine.activity.behaviour.task;

import com.leorces.common.utils.RelativeTimeParser;
import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.RetryActivityCommand;
import com.leorces.engine.configuration.properties.ActivityProperties;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.configuration.properties.ProcessProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ExternalTaskBehavior extends AbstractActivityBehavior {

    private static final int DEFAULT_TIMEOUT_IN_HOURS = 1;
    private static final int DEFAULT_RETRIES = 0;

    private final EngineProperties engineProperties;

    protected ExternalTaskBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher,
                                   EngineProperties engineProperties) {
        super(activityPersistence, dispatcher);
        this.engineProperties = engineProperties;
    }

    @Override
    public void run(ActivityExecution activity) {
        activityPersistence.schedule(enrichActivity(activity));
    }

    @Override
    public boolean fail(ActivityExecution activity) {
        int maxRetries = resolveRetries(activity);

        if (activity.retries() < maxRetries) {
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

    private ActivityExecution enrichActivity(ActivityExecution activity) {
        return activity.toBuilder()
                .timeout(resolveTimeout(activity))
                .build();
    }

    private ActivityExecution incrementRetries(ActivityExecution activity) {
        return activity.toBuilder()
                .retries(activity.retries() + 1)
                .build();
    }

    private int resolveRetries(ActivityExecution activity) {
        var definition = (ExternalTask) activity.definition();

        if (definition.retries() != null) {
            return definition.retries();
        }

        return Optional.ofNullable(resolveActivityProperties(activity))
                .map(ActivityProperties::retries)
                .orElseGet(() -> Optional.ofNullable(resolveProcessProperties(activity))
                        .map(ProcessProperties::activityRetries)
                        .orElse(DEFAULT_RETRIES));
    }

    private LocalDateTime resolveTimeout(ActivityExecution activity) {
        var definition = (ExternalTask) activity.definition();

        if (definition.timeout() != null) {
            return RelativeTimeParser.parseRelative(definition.timeout());
        }

        return Optional.ofNullable(resolveActivityProperties(activity))
                .map(ActivityProperties::timeout)
                .map(RelativeTimeParser::parseRelative)
                .orElseGet(() -> Optional.ofNullable(resolveProcessProperties(activity))
                        .map(ProcessProperties::activityTimeout)
                        .map(RelativeTimeParser::parseRelative)
                        .orElse(LocalDateTime.now().plusHours(DEFAULT_TIMEOUT_IN_HOURS)));
    }

    private ProcessProperties resolveProcessProperties(ActivityExecution activity) {
        return Optional.ofNullable(engineProperties.processes())
                .map(map -> map.get(activity.processDefinitionKey()))
                .orElse(null);
    }

    private ActivityProperties resolveActivityProperties(ActivityExecution activity) {
        return Optional.ofNullable(resolveProcessProperties(activity))
                .map(processProperties -> processProperties.activities().get(((ExternalTask) activity.definition()).topic()))
                .orElse(null);
    }

}
