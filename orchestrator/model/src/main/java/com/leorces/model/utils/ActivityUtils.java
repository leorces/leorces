package com.leorces.model.utils;


import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public final class ActivityUtils {

    private ActivityUtils() {
        // Utility class - prevent instantiation
    }

    public static List<String> buildScope(ActivityExecution activity) {
        var parentActivityDefinitionIds = findParentActivities(activity).stream()
                .map(ActivityDefinition::id)
                .toList()
                .reversed();
        return Stream.concat(parentActivityDefinitionIds.stream(), Stream.of(activity.processDefinitionId()))
                .toList();
    }

    public static boolean isAsync(ActivityExecution activity) {
        var parentActivities = findParentActivities(activity);
        return parentActivities.stream()
                .anyMatch(definition -> ActivityType.EVENT_SUBPROCESS.equals(definition.type()));
    }

    public static List<ActivityDefinition> findParentActivities(ActivityExecution activity) {
        var activityDefinition = activity.definition();
        var processDefinition = activity.processDefinition();

        var parentActivities = new ArrayList<ActivityDefinition>();
        var currentActivity = activityDefinition;

        while (currentActivity.parentId() != null) {
            var parentActivity = findActivityById(processDefinition, currentActivity.parentId());

            if (parentActivity.isEmpty()) {
                break;
            }

            var parent = parentActivity.get();
            parentActivities.add(parent);
            currentActivity = parent;
        }

        parentActivities.add(activityDefinition);
        return parentActivities;
    }

    private static Optional<ActivityDefinition> findActivityById(ProcessDefinition processDefinition, String activityId) {
        return processDefinition.activities().stream()
                .filter(activity -> activity.id().equals(activityId))
                .findFirst();
    }

}