package com.leorces.engine;

import com.leorces.api.ActivityService;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
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
    private final EngineEventBus eventBus;

    @Override
    public void run(String activityDefinitionId, String processId) {
        log.debug("Run activity by definition id: {} and process id: {}", activityDefinitionId, processId);
        eventBus.publish(ActivityEvent.runByDefinitionIdAsync(activityDefinitionId, processId));
    }

    @Override
    public void complete(String activityId) {
        complete(activityId, Map.of());
    }

    @Override
    public void complete(String activityId, Map<String, Object> variables) {
        log.debug("Complete activity by id: {} with variables: {}", activityId, variables);
        eventBus.publish(ActivityEvent.completeByIdAsync(activityId, variables));
    }

    @Override
    public void fail(String activityId) {
        fail(activityId, Map.of());
    }

    @Override
    public void fail(String activityId, Map<String, Object> variables) {
        log.debug("Fail activity by id: {} with variables: {}", activityId, variables);
        eventBus.publish(ActivityEvent.failByIdAsync(activityId, variables));
    }

    @Override
    public void terminate(String activityId) {
        log.debug("Terminate activity by id: {}", activityId);
        eventBus.publish(ActivityEvent.terminateByIdAsync(activityId));
    }

    @Override
    public void retry(String activityId) {
        log.debug("Retry activity by id: {}", activityId);
        eventBus.publish(ActivityEvent.retryByIdAsync(activityId));
    }

    @Override
    public List<Activity> poll(String topic, String processDefinitionKey, int limit) {
        return activityPersistence.poll(topic, processDefinitionKey, limit);
    }

}
