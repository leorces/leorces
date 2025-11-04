package com.leorces.model.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityDefinitionDeserializer;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
public record ProcessDefinition(
        String id,
        String key,
        String name,
        Integer version,
        @JsonDeserialize(contentUsing = ActivityDefinitionDeserializer.class)
        List<ActivityDefinition> activities,
        List<String> messages,
        List<ErrorItem> errors,
        ProcessDefinitionMetadata metadata,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    @JsonIgnore
    public Optional<ActivityDefinition> getStartActivity() {
        return activities.stream()
                .filter(activity -> ActivityType.START_EVENT.equals(activity.type()))
                .filter(activity -> activity.parentId() == null)
                .findFirst();
    }

    @JsonIgnore
    public Optional<ActivityDefinition> getActivityById(String definitionId) {
        return activities.stream()
                .filter(activity -> activity.id().equals(definitionId))
                .findFirst();
    }

    @JsonIgnore
    public List<String> scope(String activityDefinitionId) {
        var activityDefinition = getActivityById(activityDefinitionId)
                .orElseThrow(() -> new IllegalArgumentException("Activity definition not found: %s".formatted(activityDefinitionId)));

        var scopeActivities = new ArrayList<String>();
        scopeActivities.add(activityDefinitionId);

        var parentActivityDefinitionIds = findParentActivities(activityDefinition).stream()
                .map(ActivityDefinition::id)
                .toList();

        scopeActivities.addAll(parentActivityDefinitionIds);
        scopeActivities.add(id);

        return scopeActivities;
    }

    private List<ActivityDefinition> findParentActivities(ActivityDefinition activityDefinition) {
        var parentActivities = new ArrayList<ActivityDefinition>();
        var currentActivity = activityDefinition;

        // Traverse up the parent chain until we reach the root (activity with no parent)
        while (currentActivity.parentId() != null) {
            var parentActivity = getActivityById(currentActivity.parentId());

            if (parentActivity.isEmpty()) {
                // Parent activity not found, break the chain
                break;
            }

            var parent = parentActivity.get();
            parentActivities.add(parent);
            currentActivity = parent;
        }

        return parentActivities;
    }

}
