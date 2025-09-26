package com.leorces.model.runtime.process;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessExecution Tests")
class ProcessExecutionTest {

    private static final String TEST_ID = "execution-123";
    private static final String TEST_ROOT_PROCESS_ID = "root-process-456";
    private static final String TEST_PARENT_ID = "parent-789";
    private static final String TEST_BUSINESS_KEY = "business-key-001";
    private static final ProcessState TEST_STATE = ProcessState.ACTIVE;
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 11, 0);
    private static final LocalDateTime TEST_STARTED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime TEST_COMPLETED_AT = LocalDateTime.of(2024, 1, 15, 11, 30);
    private static final String TEST_DEFINITION_ID = "definition-123";
    private static final String TEST_DEFINITION_KEY = "test-process";

    @Test
    @DisplayName("Should create ProcessExecution with builder pattern")
    void shouldCreateProcessExecutionWithBuilder() {
        // Given
        var definition = createTestProcessDefinition();
        var variable1 = Variable.builder()
                .id("var1")
                .varKey("key1")
                .varValue("value1")
                .build();
        var variable2 = Variable.builder()
                .id("var2")
                .varKey("key2")
                .varValue("value2")
                .build();
        var variables = List.of(variable1, variable2);

        var activity1 = createTestActivity("activity1", "def1");
        var activity2 = createTestActivity("activity2", "def2");
        var activities = List.of(activity1, activity2);

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .rootProcessId(TEST_ROOT_PROCESS_ID)
                .parentId(TEST_PARENT_ID)
                .businessKey(TEST_BUSINESS_KEY)
                .variables(variables)
                .activities(activities)
                .state(TEST_STATE)
                .definition(definition)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .startedAt(TEST_STARTED_AT)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotNull(processExecution);
        assertEquals(TEST_ID, processExecution.id());
        assertEquals(TEST_ROOT_PROCESS_ID, processExecution.rootProcessId());
        assertEquals(TEST_PARENT_ID, processExecution.parentId());
        assertEquals(TEST_BUSINESS_KEY, processExecution.businessKey());
        assertEquals(variables, processExecution.variables());
        assertEquals(activities, processExecution.activities());
        assertEquals(TEST_STATE, processExecution.state());
        assertEquals(definition, processExecution.definition());
        assertEquals(TEST_CREATED_AT, processExecution.createdAt());
        assertEquals(TEST_UPDATED_AT, processExecution.updatedAt());
        assertEquals(TEST_STARTED_AT, processExecution.startedAt());
        assertEquals(TEST_COMPLETED_AT, processExecution.completedAt());
    }

    @Test
    @DisplayName("Should create ProcessExecution with minimal required fields")
    void shouldCreateProcessExecutionWithMinimalFields() {
        // Given
        var definition = createTestProcessDefinition();

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // Then
        assertNotNull(processExecution);
        assertEquals(TEST_ID, processExecution.id());
        assertEquals(TEST_STATE, processExecution.state());
        assertEquals(definition, processExecution.definition());
        assertNull(processExecution.rootProcessId());
        assertNull(processExecution.parentId());
        assertNull(processExecution.businessKey());
        assertNull(processExecution.variables());
        assertNull(processExecution.activities());
        assertNull(processExecution.createdAt());
        assertNull(processExecution.updatedAt());
        assertNull(processExecution.startedAt());
        assertNull(processExecution.completedAt());
    }

    @Test
    @DisplayName("Should handle empty variables and activities lists")
    void shouldHandleEmptyVariablesAndActivitiesLists() {
        // Given
        var definition = createTestProcessDefinition();
        var emptyVariables = List.<Variable>of();
        var emptyActivities = List.<Activity>of();

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .variables(emptyVariables)
                .activities(emptyActivities)
                .build();

        // Then
        assertNotNull(processExecution);
        assertEquals(emptyVariables, processExecution.variables());
        assertEquals(emptyActivities, processExecution.activities());
        assertTrue(processExecution.variables().isEmpty());
        assertTrue(processExecution.activities().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple activities with different states")
    void shouldHandleMultipleActivitiesWithDifferentStates() {
        // Given
        var definition = createTestProcessDefinition();
        var scheduledActivity = Activity.builder()
                .id("scheduled-activity")
                .definitionId("def1")
                .state(ActivityState.SCHEDULED)
                .retries(0)
                .build();

        var activeActivity = Activity.builder()
                .id("active-activity")
                .definitionId("def2")
                .state(ActivityState.ACTIVE)
                .retries(1)
                .build();

        var completedActivity = Activity.builder()
                .id("completed-activity")
                .definitionId("def3")
                .state(ActivityState.COMPLETED)
                .retries(0)
                .build();

        var activities = List.of(scheduledActivity, activeActivity, completedActivity);

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .activities(activities)
                .build();

        // Then
        assertNotNull(processExecution);
        assertEquals(3, processExecution.activities().size());
        assertEquals(scheduledActivity, processExecution.activities().get(0));
        assertEquals(activeActivity, processExecution.activities().get(1));
        assertEquals(completedActivity, processExecution.activities().get(2));
    }

    @Test
    @DisplayName("Should handle different process states")
    void shouldHandleDifferentProcessStates() {
        // Given
        var definition = createTestProcessDefinition();

        // When
        var activeExecution = ProcessExecution.builder()
                .id("active-execution")
                .state(ProcessState.ACTIVE)
                .definition(definition)
                .build();

        var completedExecution = ProcessExecution.builder()
                .id("completed-execution")
                .state(ProcessState.COMPLETED)
                .definition(definition)
                .build();

        var terminatedExecution = ProcessExecution.builder()
                .id("terminated-execution")
                .state(ProcessState.TERMINATED)
                .definition(definition)
                .build();

        // Then
        assertEquals(ProcessState.ACTIVE, activeExecution.state());
        assertEquals(ProcessState.COMPLETED, completedExecution.state());
        assertEquals(ProcessState.TERMINATED, terminatedExecution.state());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var definition = createTestProcessDefinition();
        var activity = createTestActivity("activity1", "def1");
        var activities = List.of(activity);

        var originalExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(ProcessState.ACTIVE)
                .definition(definition)
                .businessKey(TEST_BUSINESS_KEY)
                .activities(activities)
                .build();

        // When
        var modifiedExecution = originalExecution.toBuilder()
                .state(ProcessState.COMPLETED)
                .completedAt(TEST_COMPLETED_AT)
                .activities(List.of())
                .build();

        // Then
        assertNotEquals(originalExecution, modifiedExecution);
        assertEquals(TEST_ID, modifiedExecution.id());
        assertEquals(ProcessState.COMPLETED, modifiedExecution.state());
        assertEquals(definition, modifiedExecution.definition());
        assertEquals(TEST_BUSINESS_KEY, modifiedExecution.businessKey());
        assertEquals(TEST_COMPLETED_AT, modifiedExecution.completedAt());
        assertTrue(modifiedExecution.activities().isEmpty());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var activities = List.of(createTestActivity("activity1", "def1"));

        var execution1 = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .activities(activities)
                .build();

        var execution2 = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .activities(activities)
                .build();

        var execution3 = ProcessExecution.builder()
                .id("different-id")
                .state(TEST_STATE)
                .definition(definition)
                .activities(activities)
                .build();

        // When & Then
        assertEquals(execution1, execution2);
        assertNotEquals(execution1, execution3);
        assertNotEquals(null, execution1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var activities = List.of(createTestActivity("activity1", "def1"));

        var execution1 = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .activities(activities)
                .build();

        var execution2 = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .activities(activities)
                .build();

        // When & Then
        assertEquals(execution1.hashCode(), execution2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .businessKey(TEST_BUSINESS_KEY)
                .state(TEST_STATE)
                .definition(definition)
                .build();

        // When
        var toStringResult = processExecution.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ProcessExecution"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_BUSINESS_KEY));
        assertTrue(toStringResult.contains(TEST_STATE.toString()));
    }

    @Test
    @DisplayName("Should handle null collections gracefully")
    void shouldHandleNullCollectionsGracefully() {
        // Given
        var definition = createTestProcessDefinition();

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .variables(null)
                .activities(null)
                .build();

        // Then
        assertNotNull(processExecution);
        assertNull(processExecution.variables());
        assertNull(processExecution.activities());
    }

    @Test
    @DisplayName("Should handle LocalDateTime fields correctly")
    void shouldHandleLocalDateTimeFieldsCorrectly() {
        // Given
        var definition = createTestProcessDefinition();
        var now = LocalDateTime.now();

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .createdAt(now)
                .updatedAt(now)
                .startedAt(now)
                .completedAt(now)
                .build();

        // Then
        assertNotNull(processExecution);
        assertEquals(now, processExecution.createdAt());
        assertEquals(now, processExecution.updatedAt());
        assertEquals(now, processExecution.startedAt());
        assertEquals(now, processExecution.completedAt());
    }

    @Test
    @DisplayName("Should handle large collections efficiently")
    void shouldHandleLargeCollectionsEfficiently() {
        // Given
        var definition = createTestProcessDefinition();
        var variables = List.of(
                Variable.builder().id("var1").varKey("key1").varValue("value1").build(),
                Variable.builder().id("var2").varKey("key2").varValue("value2").build(),
                Variable.builder().id("var3").varKey("key3").varValue("value3").build()
        );
        var activities = List.of(
                createTestActivity("activity1", "def1"),
                createTestActivity("activity2", "def2"),
                createTestActivity("activity3", "def3"),
                createTestActivity("activity4", "def4"),
                createTestActivity("activity5", "def5")
        );

        // When
        var processExecution = ProcessExecution.builder()
                .id(TEST_ID)
                .state(TEST_STATE)
                .definition(definition)
                .variables(variables)
                .activities(activities)
                .build();

        // Then
        assertNotNull(processExecution);
        assertEquals(3, processExecution.variables().size());
        assertEquals(5, processExecution.activities().size());
        assertEquals("var1", processExecution.variables().getFirst().id());
        assertEquals("activity5", processExecution.activities().getLast().id());
    }

    private ProcessDefinition createTestProcessDefinition() {
        return ProcessDefinition.builder()
                .id(TEST_DEFINITION_ID)
                .key(TEST_DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .build();
    }

    private Activity createTestActivity(String id, String definitionId) {
        return Activity.builder()
                .id(id)
                .definitionId(definitionId)
                .state(ActivityState.ACTIVE)
                .retries(0)
                .build();
    }

}