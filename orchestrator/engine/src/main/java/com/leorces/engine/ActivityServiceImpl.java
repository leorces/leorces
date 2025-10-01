package com.leorces.engine;

import com.leorces.api.ActivityService;
import com.leorces.engine.activity.command.*;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.persistence.ActivityPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void run(String definitionId, String processId) {
        log.debug("Run activity by definition id: {} and process id: {}", definitionId, processId);
        dispatcher.dispatchAsync(RunActivityCommand.of(definitionId, processId));
    }

    @Override
    public void complete(String activityId) {
        complete(activityId, Map.of());
    }

    @Override
    public void complete(String activityId, Map<String, Object> variables) {
        log.debug("Complete activity by id: {} with variables: {}", activityId, variables);
        dispatcher.dispatchAsync(CompleteActivityCommand.of(activityId, variables));
    }

    @Override
    public void fail(String activityId) {
        fail(activityId, Map.of());
    }

    @Override
    public void fail(String activityId, Map<String, Object> variables) {
        log.debug("Fail activity by id: {} with variables: {}", activityId, variables);
        dispatcher.dispatchAsync(FailActivityCommand.of(activityId, variables));
    }

    @Override
    public void terminate(String activityId) {
        log.debug("Terminate activity by id: {}", activityId);
        dispatcher.dispatchAsync(TerminateActivityCommand.of(activityId));
    }

    @Override
    public void retry(String activityId) {
        log.debug("Retry activity by id: {}", activityId);
        dispatcher.dispatchAsync(RetryActivityCommand.of(activityId));
    }

    @Override
    public List<Activity> poll(String topic, String processDefinitionKey, int limit) {
        return activityPersistence.poll(topic, processDefinitionKey, limit);
    }

}
