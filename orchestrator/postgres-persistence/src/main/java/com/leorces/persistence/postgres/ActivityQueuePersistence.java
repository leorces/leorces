package com.leorces.persistence.postgres;

import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.postgres.entity.ActivityQueueEntity;
import com.leorces.persistence.postgres.repository.ActivityQueueRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ActivityQueuePersistence {

    private final ActivityQueueRepository activityQueueRepository;

    @Transactional
    public void push(ActivityExecution activity) {
        activityQueueRepository.save(buildActivityQueueItem(activity));
    }

    @Transactional
    public List<String> poll(String topic, String processDefinitionKey, int limit) {
        var activityIds = activityQueueRepository.findAll(topic, processDefinitionKey, limit);

        if (activityIds.isEmpty()) {
            return Collections.emptyList();
        }

        activityQueueRepository.deleteAllById(activityIds);
        return activityIds;
    }

    private ActivityQueueEntity buildActivityQueueItem(ActivityExecution activity) {
        var currentTime = LocalDateTime.now();
        var topic = getTopic(activity);
        return buildQueueEntity(activity, topic, currentTime);
    }

    private String getTopic(ActivityExecution activity) {
        var externalTask = (ExternalTask) activity.getDefinition();
        return externalTask.topic();
    }

    private ActivityQueueEntity buildQueueEntity(ActivityExecution activity, String topic, LocalDateTime currentTime) {
        return ActivityQueueEntity.builder()
                .isNew(true)
                .topic(topic)
                .processDefinitionKey(activity.processDefinitionKey())
                .activityId(activity.id())
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

}
