package com.leorces.model.utils;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ActivityUtils {

    private ActivityUtils() {
        // Utility class - prevent instantiation
    }

    public static List<String> buildScope(ActivityExecution activity) {
        var parentsAndSelf = findParentActivities(activity).stream()
                .map(ActivityDefinition::id)
                .toList();
        var parentsOnly = parentsAndSelf.isEmpty() ? List.<String>of() : parentsAndSelf.subList(0, parentsAndSelf.size() - 1);
        var scope = new ArrayList<String>(parentsOnly.size() + 2);
        scope.add(activity.definitionId());
        scope.addAll(parentsOnly);
        scope.add(activity.processDefinitionId());
        return scope;
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

        while (currentActivity != null && currentActivity.parentId() != null) {
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