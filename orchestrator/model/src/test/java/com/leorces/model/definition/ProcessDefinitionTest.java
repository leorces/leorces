package com.leorces.model.definition;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessDefinition Tests")
class ProcessDefinitionTest {

    private static final String TEST_ID = "process-def-123";
    private static final String TEST_KEY = "test-process";
    private static final String TEST_NAME = "Test Process Definition";
    private static final Integer TEST_VERSION = 1;
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 11, 0);

    @Test
    @DisplayName("Should create ProcessDefinition with builder pattern")
    void shouldCreateProcessDefinitionWithBuilder() {
        // Given
        var startActivity = createTestActivityDefinition("start-1", ActivityType.START_EVENT, null);
        var taskActivity = createTestActivityDefinition("task-1", ActivityType.EXTERNAL_TASK, null);
        var activities = List.of(startActivity, taskActivity);
        var messages = List.of("message1", "message2");
        var errors = List.of(
                ErrorItem.builder().name("error1").errorCode("E001").message("Error 1").build(),
                ErrorItem.builder().name("error2").errorCode("E002").message("Error 2").build()
        );
        var metadata = ProcessDefinitionMetadata.builder()
                .schema("BPMN")
                .origin("Test")
                .deployment("deployment-1")
                .build();

        // When
        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .messages(messages)
                .errors(errors)
                .metadata(metadata)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .build();

        // Then
        assertNotNull(processDefinition);
        assertEquals(TEST_ID, processDefinition.id());
        assertEquals(TEST_KEY, processDefinition.key());
        assertEquals(TEST_NAME, processDefinition.name());
        assertEquals(TEST_VERSION, processDefinition.version());
        assertEquals(activities, processDefinition.activities());
        assertEquals(messages, processDefinition.messages());
        assertEquals(errors, processDefinition.errors());
        assertEquals(metadata, processDefinition.metadata());
        assertEquals(TEST_CREATED_AT, processDefinition.createdAt());
        assertEquals(TEST_UPDATED_AT, processDefinition.updatedAt());
    }

    @Test
    @DisplayName("Should create ProcessDefinition with minimal fields")
    void shouldCreateProcessDefinitionWithMinimalFields() {
        // Given & When
        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .build();

        // Then
        assertNotNull(processDefinition);
        assertEquals(TEST_ID, processDefinition.id());
        assertEquals(TEST_KEY, processDefinition.key());
        assertEquals(TEST_NAME, processDefinition.name());
        assertEquals(TEST_VERSION, processDefinition.version());
        assertNull(processDefinition.activities());
        assertNull(processDefinition.messages());
        assertNull(processDefinition.errors());
        assertNull(processDefinition.metadata());
        assertNull(processDefinition.createdAt());
        assertNull(processDefinition.updatedAt());
    }

    @Test
    @DisplayName("Should find start activity")
    void shouldFindStartActivity() {
        // Given
        var startActivity = createTestActivityDefinition("start-1", ActivityType.START_EVENT, null);
        var taskActivity = createTestActivityDefinition("task-1", ActivityType.EXTERNAL_TASK, null);
        var endActivity = createTestActivityDefinition("end-1", ActivityType.END_EVENT, null);
        var activities = List.of(startActivity, taskActivity, endActivity);

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var startActivityResult = processDefinition.getStartActivity();

        // Then
        assertTrue(startActivityResult.isPresent());
        assertEquals("start-1", startActivityResult.get().id());
        assertEquals(ActivityType.START_EVENT, startActivityResult.get().type());
    }

    @Test
    @DisplayName("Should return empty when no start activity found")
    void shouldReturnEmptyWhenNoStartActivityFound() {
        // Given
        var taskActivity = createTestActivityDefinition("task-1", ActivityType.EXTERNAL_TASK, null);
        var endActivity = createTestActivityDefinition("end-1", ActivityType.END_EVENT, null);
        var activities = List.of(taskActivity, endActivity);

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var startActivityResult = processDefinition.getStartActivity();

        // Then
        assertFalse(startActivityResult.isPresent());
    }

    @Test
    @DisplayName("Should get activity by ID")
    void shouldGetActivityById() {
        // Given
        var startActivity = createTestActivityDefinition("start-1", ActivityType.START_EVENT, null);
        var taskActivity = createTestActivityDefinition("task-1", ActivityType.EXTERNAL_TASK, null);
        var activities = List.of(startActivity, taskActivity);

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var activityResult = processDefinition.getActivityById("task-1");

        // Then
        assertTrue(activityResult.isPresent());
        assertEquals("task-1", activityResult.get().id());
        assertEquals(ActivityType.EXTERNAL_TASK, activityResult.get().type());
    }

    @Test
    @DisplayName("Should return empty when activity ID not found")
    void shouldReturnEmptyWhenActivityIdNotFound() {
        // Given
        var startActivity = createTestActivityDefinition("start-1", ActivityType.START_EVENT, null);
        var activities = List.of(startActivity);

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var activityResult = processDefinition.getActivityById("non-existent");

        // Then
        assertFalse(activityResult.isPresent());
    }

    @Test
    @DisplayName("Should build scope for activity")
    void shouldBuildScopeForActivity() {
        // Given
        var parentActivity = createTestActivityDefinition("parent-1", ActivityType.SUBPROCESS, null);
        var childActivity = createTestActivityDefinition("child-1", ActivityType.EXTERNAL_TASK, "parent-1");
        var activities = List.of(parentActivity, childActivity);

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var scope = processDefinition.scope("child-1");

        // Then
        assertNotNull(scope);
        assertEquals(3, scope.size());
        assertEquals("child-1", scope.get(0));
        assertEquals("parent-1", scope.get(1));
        assertEquals(TEST_ID, scope.get(2));
    }

    @Test
    @DisplayName("Should throw exception when building scope for non-existent activity")
    void shouldThrowExceptionWhenBuildingScopeForNonExistentActivity() {
        // Given
        var activities = List.of(
                createTestActivityDefinition("start-1", ActivityType.START_EVENT, null)
        );

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                processDefinition.scope("non-existent"));
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var activities = List.of(
                createTestActivityDefinition("start-1", ActivityType.START_EVENT, null)
        );
        var originalDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var modifiedDefinition = originalDefinition.toBuilder()
                .name("Modified Process")
                .version(2)
                .build();

        // Then
        assertNotEquals(originalDefinition, modifiedDefinition);
        assertEquals(TEST_ID, modifiedDefinition.id());
        assertEquals(TEST_KEY, modifiedDefinition.key());
        assertEquals("Modified Process", modifiedDefinition.name());
        assertEquals(2, modifiedDefinition.version());
        assertEquals(activities, modifiedDefinition.activities());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var activities = List.of(
                createTestActivityDefinition("start-1", ActivityType.START_EVENT, null)
        );

        var definition1 = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        var definition2 = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        var definition3 = ProcessDefinition.builder()
                .id("different-id")
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When & Then
        assertEquals(definition1, definition2);
        assertNotEquals(definition1, definition3);
        assertNotEquals(null, definition1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var activities = List.of(
                createTestActivityDefinition("start-1", ActivityType.START_EVENT, null)
        );

        var definition1 = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        var definition2 = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When & Then
        assertEquals(definition1.hashCode(), definition2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .build();

        // When
        var toStringResult = processDefinition.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ProcessDefinition"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_KEY));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_VERSION.toString()));
    }

    @Test
    @DisplayName("Should handle complex hierarchical scope")
    void shouldHandleComplexHierarchicalScope() {
        // Given
        var grandParent = createTestActivityDefinition("grandparent", ActivityType.SUBPROCESS, null);
        var parent = createTestActivityDefinition("parent", ActivityType.SUBPROCESS, "grandparent");
        var child = createTestActivityDefinition("child", ActivityType.EXTERNAL_TASK, "parent");
        var activities = List.of(grandParent, parent, child);

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(activities)
                .build();

        // When
        var scope = processDefinition.scope("child");

        // Then
        assertNotNull(scope);
        assertEquals(4, scope.size());
        assertEquals("child", scope.get(0));
        assertEquals("parent", scope.get(1));
        assertEquals("grandparent", scope.get(2));
        assertEquals(TEST_ID, scope.get(3));
    }

    @Test
    @DisplayName("Should handle empty activities list")
    void shouldHandleEmptyActivitiesList() {
        // Given
        var emptyActivities = List.<ActivityDefinition>of();

        // When
        var processDefinition = ProcessDefinition.builder()
                .id(TEST_ID)
                .key(TEST_KEY)
                .name(TEST_NAME)
                .version(TEST_VERSION)
                .activities(emptyActivities)
                .build();

        // Then
        assertNotNull(processDefinition);
        assertEquals(emptyActivities, processDefinition.activities());
        assertTrue(processDefinition.activities().isEmpty());
        assertFalse(processDefinition.getStartActivity().isPresent());
    }

    private ActivityDefinition createTestActivityDefinition(String id, ActivityType type, String parentId) {
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
                return id + "-name";
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

}