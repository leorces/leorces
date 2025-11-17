package com.leorces.engine.activity.behaviour;

import com.leorces.common.utils.RelativeTimeParser;
import com.leorces.engine.activity.command.RetryActivityCommand;
import com.leorces.engine.configuration.properties.ActivityProperties;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.configuration.properties.ProcessProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ExternalTaskDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.time.LocalDateTime;
import java.util.Optional;

public abstract class AbstractExternalTaskBehavior extends AbstractActivityBehavior {

    private static final int DEFAULT_TIMEOUT_IN_HOURS = 1;
    private static final int DEFAULT_RETRIES = 0;

    private final EngineProperties engineProperties;

    protected AbstractExternalTaskBehavior(ActivityPersistence activityPersistence,
                                           CommandDispatcher dispatcher,
                                           EngineProperties engineProperties) {
        super(activityPersistence, dispatcher);
        this.engineProperties = engineProperties;
    }

    @Override
    public void run(ActivityExecution externalTask) {
        activityPersistence.schedule(enrichActivity(externalTask));
    }

    @Override
    public boolean fail(ActivityExecution externalTask) {
        int maxRetries = resolveRetries(externalTask);

        if (externalTask.retries() < maxRetries) {
            dispatcher.dispatchAsync(RetryActivityCommand.of(externalTask));
            return false;
        }

        activityPersistence.fail(externalTask);
        return true;
    }

    @Override
    public void retry(ActivityExecution externalTask) {
        activityPersistence.schedule(incrementRetries(externalTask));
    }

    private ActivityExecution enrichActivity(ActivityExecution externalTask) {
        return externalTask.toBuilder()
                .timeout(resolveTimeout(externalTask))
                .build();
    }

    private ActivityExecution incrementRetries(ActivityExecution externalTask) {
        return externalTask.toBuilder()
                .retries(externalTask.retries() + 1)
                .build();
    }

    private int resolveRetries(ActivityExecution externalTask) {
        var definition = (ExternalTaskDefinition) externalTask.definition();

        if (definition.retries() != null) {
            return definition.retries();
        }

        return Optional.ofNullable(resolveActivityProperties(externalTask))
                .map(ActivityProperties::retries)
                .orElseGet(() -> Optional.ofNullable(resolveProcessProperties(externalTask))
                        .map(ProcessProperties::activityRetries)
                        .orElse(DEFAULT_RETRIES));
    }

    private LocalDateTime resolveTimeout(ActivityExecution externalTask) {
        var definition = (ExternalTaskDefinition) externalTask.definition();

        if (definition.timeout() != null) {
            return RelativeTimeParser.parseRelative(definition.timeout());
        }

        return Optional.ofNullable(resolveActivityProperties(externalTask))
                .map(ActivityProperties::timeout)
                .map(RelativeTimeParser::parseRelative)
                .orElseGet(() -> Optional.ofNullable(resolveProcessProperties(externalTask))
                        .map(ProcessProperties::activityTimeout)
                        .map(RelativeTimeParser::parseRelative)
                        .orElse(LocalDateTime.now().plusHours(DEFAULT_TIMEOUT_IN_HOURS)));
    }

    private ProcessProperties resolveProcessProperties(ActivityExecution externalTask) {
        return Optional.ofNullable(engineProperties.processes())
                .map(processProperties -> processProperties.get(externalTask.processDefinitionKey()))
                .orElse(null);
    }

    private ActivityProperties resolveActivityProperties(ActivityExecution externalTask) {
        return Optional.ofNullable(resolveProcessProperties(externalTask))
                .map(processProperties -> processProperties.activities().get(((ExternalTaskDefinition) externalTask.definition()).topic()))
                .orElse(null);
    }

}
