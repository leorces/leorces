package com.leorces.persistence.postgres.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.postgres.entity.ActivityEntity;
import com.leorces.persistence.postgres.entity.ActivityExecutionEntity;
import com.leorces.persistence.postgres.utils.IdGenerator;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ActivityMapper {

    private final ProcessMapper processMapper;
    private final VariableMapper variableMapper;
    private final ObjectMapper objectMapper;

    public ActivityMapper(@Lazy ProcessMapper processMapper,
                          VariableMapper variableMapper,
                          ObjectMapper objectMapper) {
        this.processMapper = processMapper;
        this.variableMapper = variableMapper;
        this.objectMapper = objectMapper;
    }

    public ActivityExecutionEntity toExecutionEntity(ActivityExecution activity) {
        return ActivityExecutionEntity.builder()
                .isNew(activity.id() == null)
                .id(activity.id() == null ? IdGenerator.getNewId() : activity.id())
                .processId(activity.processId())
                .activityDefinitionId(activity.definitionId())
                .parentActivityDefinitionId(activity.parentDefinitionId())
                .processDefinitionId(activity.processDefinitionId())
                .processDefinitionKey(activity.processDefinitionKey())
                .type(activity.type().name())
                .state(activity.state().name())
                .topic(getTopic(activity))
                .retries(activity.retries())
                .timeout(activity.timeout())
                .failureReason(activity.failure() != null ? activity.failure().reason() : null)
                .failureTrace(activity.failure() != null ? activity.failure().trace() : null)
                .async(activity.isAsync())
                .createdAt(activity.createdAt())
                .updatedAt(activity.updatedAt())
                .startedAt(activity.startedAt())
                .completedAt(activity.completedAt())
                .build();
    }

    public ActivityExecution toExecution(ActivityExecutionEntity entity) {
        var variables = variableMapper.toVariables(entity.getVariablesJson());
        var failure = new ActivityFailure(entity.getFailureReason(), entity.getFailureTrace());
        var activity = ActivityExecution.builder()
                .id(entity.getId())
                .definitionId(entity.getActivityDefinitionId())
                .state(ActivityState.valueOf(entity.getState()))
                .retries(entity.getRetries())
                .timeout(entity.getTimeout())
                .failure(failure)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .process(processMapper.toProcess(entity))
                .build();

        return activity.toBuilder()
                .variables(mapVariables(variables, activity))
                .build();
    }

    public Activity toActivity(ActivityExecutionEntity entity) {
        var variables = variableMapper.toVariables(entity.getVariablesJson());
        var failure = new ActivityFailure(entity.getFailureReason(), entity.getFailureTrace());
        return Activity.builder()
                .id(entity.getId())
                .definitionId(entity.getDefinitionId())
                .variables(variables)
                .state(ActivityState.valueOf(entity.getState()))
                .retries(entity.getRetries())
                .timeout(entity.getTimeout())
                .failure(failure)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
//        var execution = toExecution(entity);
//        return toActivity(execution);
    }

    public List<Activity> toActivities(PGobject activitiesJson, Process process) {
        if (activitiesJson == null || activitiesJson.getValue() == null) {
            return Collections.emptyList();
        }

        try {
            var activityEntities = objectMapper.readValue(
                    activitiesJson.getValue(),
                    new TypeReference<List<ActivityEntity>>() {
                    }
            );

            return activityEntities.stream()
                    .map(entity -> toActivity(entity, process))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize activities JSON", e);
        }
    }

    private Activity toActivity(ActivityEntity entity, Process process) {
        var variables = variableMapper.toVariables(entity.getVariablesJson());
        var failure = new ActivityFailure(entity.getFailureReason(), entity.getFailureTrace());
        var execution = ActivityExecution.builder()
                .id(entity.getId())
                .definitionId(entity.getActivityDefinitionId())
                .process(process)
                .state(ActivityState.valueOf(entity.getState()))
                .retries(entity.getRetries())
                .timeout(entity.getTimeout())
                .failure(failure)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();

        return toActivity(execution).toBuilder()
                .variables(mapVariables(variables, execution))
                .build();
    }

    private List<Variable> mapVariables(List<Variable> variables, ActivityExecution activity) {
        var variableScope = activity.scope();
        return variables.stream()
                .filter(variable -> variableScope.contains(variable.executionDefinitionId()))
                .toList();
    }

    private Activity toActivity(ActivityExecution execution) {
        return Activity.builder()
                .id(execution.id())
                .definitionId(execution.definitionId())
                .variables(execution.variables())
                .state(execution.state())
                .retries(execution.retries())
                .timeout(execution.timeout())
                .failure(execution.failure())
                .createdAt(execution.createdAt())
                .updatedAt(execution.updatedAt())
                .startedAt(execution.startedAt())
                .completedAt(execution.completedAt())
                .build();
    }

    private String getTopic(ActivityExecution activity) {
        var definition = activity.definition();
        if (definition instanceof ExternalTask) {
            return ((ExternalTask) definition).topic();
        } else {
            return null;
        }
    }

}
