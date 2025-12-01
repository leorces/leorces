package com.leorces.persistence.postgres;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.VariablePersistence;
import com.leorces.persistence.postgres.mapper.ActivityMapper;
import com.leorces.persistence.postgres.repository.ActivityRepository;
import com.leorces.persistence.postgres.utils.ActivityStateTransition;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ActivityPersistenceImpl implements ActivityPersistence {

    private final VariablePersistence variablePersistence;
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ActivityExecution schedule(ActivityExecution activity) {
        log.debug("Schedule activity: {} for process: {}", activity.definitionId(), activity.processId());
        var scheduledActivity = save(activity, ActivityState.SCHEDULED);
        var newVariables = variablePersistence.save(scheduledActivity);
        return scheduledActivity.toBuilder()
                .variables(newVariables)
                .build();
    }

    @Override
    @Transactional
    public ActivityExecution run(ActivityExecution activity) {
        log.debug("Run activity: {} for process: {}", activity.definitionId(), activity.processId());
        var newActivity = save(activity, ActivityState.ACTIVE);
        var newVariables = variablePersistence.save(newActivity);
        return newActivity.toBuilder()
                .variables(newVariables)
                .build();
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        log.debug("Complete activity: {} for process: {}", activity.definitionId(), activity.processId());
        return save(activity, ActivityState.COMPLETED);
    }

    @Override
    public ActivityExecution terminate(ActivityExecution activity) {
        log.debug("Terminate activity: {} for process: {}", activity.definitionId(), activity.processId());
        return save(activity, ActivityState.TERMINATED);
    }

    @Override
    public ActivityExecution fail(ActivityExecution activity) {
        log.debug("Fail activity: {} for process: {}", activity.definitionId(), activity.processId());
        return save(activity, ActivityState.FAILED);
    }

    @Override
    public void changeState(String activityId, ActivityState state) {
        log.debug("Change activity: {} state to: {}", activityId, state);
        activityRepository.changeState(activityId, state.name());
    }

    @Override
    @Transactional
    public void deleteAllActive(String processId, List<String> definitionIds) {
        log.debug("Delete all active activities for process: {} and definition ids: {}", processId, definitionIds);
        activityRepository.deleteAllActive(processId, definitionIds.toArray(String[]::new));
    }

    @Override
    public Optional<ActivityExecution> findById(String id) {
        log.debug("Finding activity by id: {}", id);
        return activityRepository.findById(id)
                .map(activityMapper::toExecution);
    }

    @Override
    public Optional<ActivityExecution> findByDefinitionId(String processId, String definitionId) {
        log.debug("Finding activity by definition id: {} for process: {}", definitionId, processId);
        return activityRepository.findByDefinitionId(processId, definitionId).stream()
                .map(activityMapper::toExecution)
                .filter(activity -> !activity.isInTerminalState())
                .findFirst();
    }

    @Override
    public List<ActivityExecution> findActive(String processId, List<String> definitionIds) {
        log.debug("Finding all active activities for process: {} and definition ids: {}", processId, definitionIds);
        return activityRepository.findActive(processId, definitionIds.toArray(definitionIds.toArray(new String[0]))).stream()
                .map(activityMapper::toBaseExecution)
                .toList();
    }

    @Override
    public List<ActivityExecution> findActive(String processId) {
        log.debug("Finding all active activities for process: {}", processId);
        return activityRepository.findActive(processId).stream()
                .map(activityMapper::toBaseExecution)
                .toList();
    }

    @Override
    public List<ActivityExecution> findFailed(String processId) {
        log.debug("Finding all failed activities for process: {}", processId);
        return activityRepository.findFailed(processId).stream()
                .map(activityMapper::toBaseExecution)
                .toList();
    }

    @Override
    public List<ActivityExecution> findTimedOut(int limit) {
        log.debug("Finding all timed out activities with limit: {}", limit);
        return activityRepository.findTimedOut(limit).stream()
                .map(activityMapper::toExecution)
                .toList();
    }

    @Override
    @Transactional
    public List<Activity> poll(String topic, String processDefinitionKey, int limit) {
        return activityRepository.poll(topic, processDefinitionKey, limit).stream()
                .map(activityMapper::toActivity)
                .toList();
    }

    @Override
    public boolean isAnyFailed(String processId) {
        log.debug("Checking if any activity failed for process: {}", processId);
        return activityRepository.isAnyFailed(processId);
    }

    @Override
    public boolean isAllCompleted(String processId) {
        log.debug("Checking if all activity completed for process: {}", processId);
        return activityRepository.isAllCompleted(processId);
    }

    @Override
    public boolean isAllCompleted(String processId, List<String> definitionIds) {
        log.debug("Checking if all activity completed for process: {} and definition ids: {}", processId, definitionIds);
        return activityRepository.isAllCompleted(processId, definitionIds.toArray(String[]::new));
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