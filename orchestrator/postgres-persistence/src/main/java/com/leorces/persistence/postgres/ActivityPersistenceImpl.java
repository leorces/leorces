package com.leorces.persistence.postgres;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.VariablePersistence;
import com.leorces.persistence.postgres.mapper.ActivityMapper;
import com.leorces.persistence.postgres.repository.ActivityRepository;
import com.leorces.persistence.postgres.utils.ActivityStateTransition;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ActivityPersistenceImpl implements ActivityPersistence {

    private final VariablePersistence variablePersistence;
    private final ActivityQueuePersistence activityQueuePersistence;
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ActivityExecution schedule(ActivityExecution activity) {
        var scheduledActivity = save(activity, ActivityState.SCHEDULED);
        activityQueuePersistence.push(scheduledActivity);
        var newVariables = variablePersistence.save(scheduledActivity);
        return scheduledActivity.toBuilder()
                .variables(newVariables)
                .build();
    }

    @Override
    @Transactional
    public ActivityExecution run(ActivityExecution activity) {
        var newActivity = save(activity, ActivityState.ACTIVE);
        var newVariables = variablePersistence.save(newActivity);
        return newActivity.toBuilder()
                .variables(newVariables)
                .build();
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        return save(activity, ActivityState.COMPLETED);
    }

    @Override
    public ActivityExecution terminate(ActivityExecution activity) {
        return save(activity, ActivityState.TERMINATED);
    }

    @Override
    public ActivityExecution fail(ActivityExecution activity) {
        return save(activity, ActivityState.FAILED);
    }

    @Override
    public void changeState(String activityId, ActivityState state) {
        activityRepository.changeState(activityId, state.name());
    }

    @Override
    @Transactional
    public void deleteAllActive(String processId, List<String> definitionIds) {
        activityRepository.deleteAllActive(processId, definitionIds);
    }

    @Override
    public Optional<ActivityExecution> findById(String id) {
        return activityRepository.findById(id)
                .map(activityMapper::toExecution);
    }

    @Override
    public Optional<ActivityExecution> findByDefinitionId(String processId, String definitionId) {
        return activityRepository.findByDefinitionId(processId, definitionId)
                .map(activityMapper::toExecution);
    }

    @Override
    public List<ActivityExecution> findActive(String processId, List<String> definitionIds) {
        return activityRepository.findActive(processId, definitionIds).stream()
                .map(activityMapper::toExecution)
                .toList();
    }

    @Override
    public List<ActivityExecution> findActive(String processId) {
        return activityRepository.findActive(processId).stream()
                .map(activityMapper::toExecution)
                .toList();
    }

    @Override
    public List<ActivityExecution> findFailed(String processId) {
        return activityRepository.findFailed(processId).stream()
                .map(activityMapper::toExecution)
                .toList();
    }

    @Override
    public List<ActivityExecution> findTimedOut(int limit) {
        return activityRepository.findTimedOut(limit).stream()
                .map(activityMapper::toExecution)
                .toList();
    }

    @Override
    @Transactional
    public List<Activity> poll(String topic, String processDefinitionKey, int limit) {
        var activityIds = activityQueuePersistence.poll(topic, processDefinitionKey, limit);

        if (activityIds.isEmpty()) {
            return Collections.emptyList();
        }

        activityRepository.updateStatusBatch(activityIds, ProcessState.ACTIVE.name());
        return activityRepository.findAllByIds(activityIds).stream()
                .map(activityMapper::toActivity)
                .toList();
    }

    @Override
    public boolean isAnyFailed(String processId) {
        return activityRepository.isAnyFailed(processId);
    }

    @Override
    public boolean isAllCompleted(String processId, String parentDefinitionId) {
        return activityRepository.isAllCompleted(processId, parentDefinitionId);
    }

    @Override
    public boolean isAllCompleted(String processId) {
        return activityRepository.isAllCompleted(processId);
    }

    @Override
    public boolean isAllCompleted(String processId, List<String> definitionIds) {
        return activityRepository.isAllCompleted(processId, definitionIds);
    }

    private ActivityExecution save(ActivityExecution activity, ActivityState state) {
        return save(ActivityStateTransition.to(state).apply(activity));
    }

    private ActivityExecution save(ActivityExecution activity) {
        var entity = activityMapper.toExecutionEntity(activity);
        var savedEntity = activityRepository.save(entity);
        return activity.toBuilder()
                .id(savedEntity.getId())
                .build();
    }

}