package com.leorces.model.utils;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActivityUtils Tests")
class ActivityUtilsTest {

    private static final String PROCESS_ID = "process-1";
    private static final String PROCESS_DEFINITION_ID = "process-def-1";
    private static final String PARENT_ACTIVITY_ID = "parent-1";
    private static final String CHILD_ACTIVITY_ID = "child-1";

    @Test
    @DisplayName("Should build scope for activity without parents")
    void shouldBuildScopeForActivityWithoutParents() {
        // Given
        var definition = createMockActivityDefinition(CHILD_ACTIVITY_ID, null, ActivityType.EXTERNAL_TASK);
        var processDefinition = createProcessDefinition(List.of(definition));
        var activity = createActivityExecution(definition, processDefinition);

        // When
        var scope = ActivityUtils.buildScope(activity);

        // Then
        assertNotNull(scope);
        assertEquals(2, scope.size());
        assertEquals(CHILD_ACTIVITY_ID, scope.get(0));
        assertEquals(PROCESS_DEFINITION_ID, scope.get(1));
    }

    @Test
    @DisplayName("Should build scope for activity with parent")
    void shouldBuildScopeForActivityWithParent() {
        // Given
        var parentDefinition = createMockActivityDefinition(PARENT_ACTIVITY_ID, null, ActivityType.SUBPROCESS);
        var childDefinition = createMockActivityDefinition(CHILD_ACTIVITY_ID, PARENT_ACTIVITY_ID, ActivityType.EXTERNAL_TASK);
        var processDefinition = createProcessDefinition(List.of(parentDefinition, childDefinition));
        var activity = createActivityExecution(childDefinition, processDefinition);

        // When
        var scope = ActivityUtils.buildScope(activity);

        // Then
        assertNotNull(scope);
        assertEquals(3, scope.size());
        assertEquals(CHILD_ACTIVITY_ID, scope.get(0));
        assertEquals(PARENT_ACTIVITY_ID, scope.get(1));
        assertEquals(PROCESS_DEFINITION_ID, scope.get(2));
    }

    @Test
    @DisplayName("Should return false for isAsync when no EVENT_SUBPROCESS parent")
    void shouldReturnFalseForIsAsyncWhenNoEventSubprocessParent() {
        // Given
        var definition = createMockActivityDefinition(CHILD_ACTIVITY_ID, null, ActivityType.EXTERNAL_TASK);
        var processDefinition = createProcessDefinition(List.of(definition));
        var activity = createActivityExecution(definition, processDefinition);

        // When
        var isAsync = ActivityUtils.isAsync(activity);

        // Then
        assertFalse(isAsync);
    }

    @Test
    @DisplayName("Should return true for isAsync when EVENT_SUBPROCESS parent exists")
    void shouldReturnTrueForIsAsyncWhenEventSubprocessParentExists() {
        // Given
        var parentDefinition = createMockActivityDefinition(PARENT_ACTIVITY_ID, null, ActivityType.EVENT_SUBPROCESS);
        var childDefinition = createMockActivityDefinition(CHILD_ACTIVITY_ID, PARENT_ACTIVITY_ID, ActivityType.EXTERNAL_TASK);
        var processDefinition = createProcessDefinition(List.of(parentDefinition, childDefinition));
        var activity = createActivityExecution(childDefinition, processDefinition);

        // When
        var isAsync = ActivityUtils.isAsync(activity);

        // Then
        assertTrue(isAsync);
    }

    @Test
    @DisplayName("Should find parent activities correctly")
    void shouldFindParentActivitiesCorrectly() {
        // Given
        var parentDefinition = createMockActivityDefinition(PARENT_ACTIVITY_ID, null, ActivityType.SUBPROCESS);
        var childDefinition = createMockActivityDefinition(CHILD_ACTIVITY_ID, PARENT_ACTIVITY_ID, ActivityType.EXTERNAL_TASK);
        var processDefinition = createProcessDefinition(List.of(parentDefinition, childDefinition));
        var activity = createActivityExecution(childDefinition, processDefinition);

        // When
        var parentActivities = ActivityUtils.findParentActivities(activity);

        // Then
        assertNotNull(parentActivities);
        assertEquals(2, parentActivities.size());
        assertEquals(PARENT_ACTIVITY_ID, parentActivities.get(0).id());
        assertEquals(CHILD_ACTIVITY_ID, parentActivities.get(1).id());
    }

    @Test
    @DisplayName("Should return only self when activity has no parents")
    void shouldReturnOnlySelfWhenActivityHasNoParents() {
        // Given
        var definition = createMockActivityDefinition(CHILD_ACTIVITY_ID, null, ActivityType.EXTERNAL_TASK);
        var processDefinition = createProcessDefinition(List.of(definition));
        var activity = createActivityExecution(definition, processDefinition);

        // When
        var parentActivities = ActivityUtils.findParentActivities(activity);

        // Then
        assertNotNull(parentActivities);
        assertEquals(1, parentActivities.size());
        assertEquals(CHILD_ACTIVITY_ID, parentActivities.get(0).id());
    }

    private ActivityDefinition createMockActivityDefinition(String id, String parentId, ActivityType type) {
        return new ActivityDefinition() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String parentId() {
                return parentId;
            }

            @Override
            public String name() {
                return "Test Activity";
            }

            @Override
            public ActivityType type() {
                return type;
            }

            @Override
            public List<String> incoming() {
                return List.of();
            }

            @Override
            public List<String> outgoing() {
                return List.of();
            }

            @Override
            public Map<String, Object> inputs() {
                return Map.of();
            }

            @Override
            public Map<String, Object> outputs() {
                return Map.of();
            }
        };
    }

    private ProcessDefinition createProcessDefinition(List<ActivityDefinition> activities) {
        return ProcessDefinition.builder()
                .id(PROCESS_DEFINITION_ID)
                .key("test-process")
                .name("Test Process")
                .version(1)
                .activities(activities)
                .build();
    }

    private ActivityExecution createActivityExecution(ActivityDefinition definition, ProcessDefinition processDefinition) {
        var process = Process.builder()
                .id(PROCESS_ID)
                .state(ProcessState.ACTIVE)
                .definition(processDefinition)
                .build();

        return ActivityExecution.builder()
                .id("execution-1")
                .definitionId(definition.id())
                .process(process)
                .state(ActivityState.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

}