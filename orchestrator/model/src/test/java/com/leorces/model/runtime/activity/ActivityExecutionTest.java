package com.leorces.model.runtime.activity;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActivityExecution Tests")
class ActivityExecutionTest {

    private static final String TEST_ID = "execution-123";
    private static final String TEST_DEFINITION_ID = "definition-456";
    private static final String TEST_PROCESS_ID = "process-789";
    private static final String TEST_PROCESS_DEFINITION_ID = "process-def-001";
    private static final String TEST_PROCESS_DEFINITION_KEY = "test-process";
    private static final ActivityState TEST_STATE = ActivityState.ACTIVE;
    private static final int TEST_RETRIES = 3;
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 11, 0);
    private static final LocalDateTime TEST_STARTED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime TEST_COMPLETED_AT = LocalDateTime.of(2024, 1, 15, 11, 30);

    @Test
    @DisplayName("Should create ActivityExecution with builder pattern")
    void shouldCreateActivityExecutionWithBuilder() {
        // Given
        var process = createTestProcess();
        var variable1 = Variable.builder()
                .id("var1")
                .varKey("key1")
                .varValue("value1")
                .build();
        var variables = List.of(variable1);

        // When
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .variables(variables)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .startedAt(TEST_STARTED_AT)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotNull(activityExecution);
        assertEquals(TEST_ID, activityExecution.id());
        assertEquals(TEST_DEFINITION_ID, activityExecution.definitionId());
        assertEquals(process, activityExecution.process());
        assertEquals(variables, activityExecution.variables());
        assertEquals(TEST_STATE, activityExecution.state());
        assertEquals(TEST_RETRIES, activityExecution.retries());
        assertEquals(TEST_CREATED_AT, activityExecution.createdAt());
        assertEquals(TEST_UPDATED_AT, activityExecution.updatedAt());
        assertEquals(TEST_STARTED_AT, activityExecution.startedAt());
        assertEquals(TEST_COMPLETED_AT, activityExecution.completedAt());
    }

    @Test
    @DisplayName("Should return empty list when variables is null")
    void shouldReturnEmptyListWhenVariablesIsNull() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .variables(null)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var variables = activityExecution.variables();

        // Then
        assertNotNull(variables);
        assertTrue(variables.isEmpty());
    }

    @Test
    @DisplayName("Should return activity definition")
    void shouldReturnActivityDefinition() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var definition = activityExecution.definition();

        // Then
        assertNotNull(definition);
        assertEquals(TEST_DEFINITION_ID, definition.id());
        assertEquals("Test Activity", definition.name());
        assertEquals(ActivityType.EXTERNAL_TASK, definition.type());
    }

    @Test
    @DisplayName("Should return process definition")
    void shouldReturnProcessDefinition() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var processDefinition = activityExecution.processDefinition();

        // Then
        assertNotNull(processDefinition);
        assertEquals(TEST_PROCESS_DEFINITION_ID, processDefinition.id());
        assertEquals(TEST_PROCESS_DEFINITION_KEY, processDefinition.key());
    }

    @Test
    @DisplayName("Should return process definition ID")
    void shouldReturnProcessDefinitionId() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var processDefinitionId = activityExecution.processDefinitionId();

        // Then
        assertEquals(TEST_PROCESS_DEFINITION_ID, processDefinitionId);
    }

    @Test
    @DisplayName("Should return process definition key")
    void shouldReturnProcessDefinitionKey() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var processDefinitionKey = activityExecution.processDefinitionKey();

        // Then
        assertEquals(TEST_PROCESS_DEFINITION_KEY, processDefinitionKey);
    }

    @Test
    @DisplayName("Should return activity type")
    void shouldReturnActivityType() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var type = activityExecution.type();

        // Then
        assertEquals(ActivityType.EXTERNAL_TASK, type);
    }

    @Test
    @DisplayName("Should return process ID")
    void shouldReturnProcessId() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var processId = activityExecution.processId();

        // Then
        assertEquals(TEST_PROCESS_ID, processId);
    }

    @Test
    @DisplayName("Should return next activities")
    void shouldReturnNextActivities() {
        // Given
        var nextActivityDef = new ActivityDefinition() {
            @Override
            public String id() {
                return "outgoing1";
            }

            @Override
            public String parentId() {
                return null;
            }

            @Override
            public String name() {
                return "Next Activity";
            }

            @Override
            public ActivityType type() {
                return ActivityType.END_EVENT;
            }

            @Override
            public List<String> incoming() {
                return List.of(TEST_DEFINITION_ID);
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

        var processDefinition = ProcessDefinition.builder()
                .id(TEST_PROCESS_DEFINITION_ID)
                .key(TEST_PROCESS_DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(createTestActivityDefinition(), nextActivityDef))
                .build();

        var process = Process.builder()
                .id(TEST_PROCESS_ID)
                .state(ProcessState.ACTIVE)
                .definition(processDefinition)
                .build();

        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var nextActivities = activityExecution.nextActivities();

        // Then
        assertNotNull(nextActivities);
        assertEquals(1, nextActivities.size());
        assertEquals("outgoing1", nextActivities.getFirst().id());
        assertEquals("Next Activity", nextActivities.getFirst().name());
    }

    @Test
    @DisplayName("Should return inputs")
    void shouldReturnInputs() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var inputs = activityExecution.inputs();

        // Then
        assertNotNull(inputs);
        assertEquals(Map.of("input1", "value1"), inputs);
    }

    @Test
    @DisplayName("Should return outputs")
    void shouldReturnOutputs() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var outputs = activityExecution.outputs();

        // Then
        assertNotNull(outputs);
        assertEquals(Map.of("output1", "result1"), outputs);
    }

    @Test
    @DisplayName("Should return outgoing")
    void shouldReturnOutgoing() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var outgoing = activityExecution.outgoing();

        // Then
        assertNotNull(outgoing);
        assertEquals(List.of("outgoing1", "outgoing2"), outgoing);
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var process = createTestProcess();
        var originalExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(ActivityState.ACTIVE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var modifiedExecution = originalExecution.toBuilder()
                .state(ActivityState.COMPLETED)
                .retries(0)
                .completedAt(TEST_COMPLETED_AT)
                .build();

        // Then
        assertNotEquals(originalExecution, modifiedExecution);
        assertEquals(TEST_ID, modifiedExecution.id());
        assertEquals(TEST_DEFINITION_ID, modifiedExecution.definitionId());
        assertEquals(process, modifiedExecution.process());
        assertEquals(ActivityState.COMPLETED, modifiedExecution.state());
        assertEquals(0, modifiedExecution.retries());
        assertEquals(TEST_COMPLETED_AT, modifiedExecution.completedAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var process = createTestProcess();
        var execution1 = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        var execution2 = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        var execution3 = ActivityExecution.builder()
                .id("different-id")
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
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
        var process = createTestProcess();
        var execution1 = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        var execution2 = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When & Then
        assertEquals(execution1.hashCode(), execution2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var process = createTestProcess();
        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId(TEST_DEFINITION_ID)
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When
        var toStringResult = activityExecution.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ActivityExecution"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_DEFINITION_ID));
        assertTrue(toStringResult.contains(TEST_STATE.toString()));
    }

    @Test
    @DisplayName("Should throw exception when definition not found")
    void shouldThrowExceptionWhenDefinitionNotFound() {
        // Given
        var processDefinition = ProcessDefinition.builder()
                .id(TEST_PROCESS_DEFINITION_ID)
                .key(TEST_PROCESS_DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of()) // Empty activities list
                .build();

        var process = Process.builder()
                .id(TEST_PROCESS_ID)
                .state(ProcessState.ACTIVE)
                .definition(processDefinition)
                .build();

        var activityExecution = ActivityExecution.builder()
                .id(TEST_ID)
                .definitionId("non-existent-definition")
                .process(process)
                .state(TEST_STATE)
                .retries(TEST_RETRIES)
                .build();

        // When & Then
        assertThrows(RuntimeException.class, activityExecution::definition);
    }

    private ActivityDefinition createTestActivityDefinition() {
        return new ActivityDefinition() {
            @Override
            public String id() {
                return TEST_DEFINITION_ID;
            }

            @Override
            public String parentId() {
                return null;
            }

            @Override
            public String name() {
                return "Test Activity";
            }

            @Override
            public ActivityType type() {
                return ActivityType.EXTERNAL_TASK;
            }

            @Override
            public List<String> incoming() {
                return List.of("incoming1");
            }

            @Override
            public List<String> outgoing() {
                return List.of("outgoing1", "outgoing2");
            }

            @Override
            public Map<String, Object> inputs() {
                return Map.of("input1", "value1");
            }

            @Override
            public Map<String, Object> outputs() {
                return Map.of("output1", "result1");
            }
        };
    }

    private ProcessDefinition createTestProcessDefinition() {
        var activityDefinition = createTestActivityDefinition();
        return ProcessDefinition.builder()
                .id(TEST_PROCESS_DEFINITION_ID)
                .key(TEST_PROCESS_DEFINITION_KEY)
                .name("Test Process")
                .version(1)
                .activities(List.of(activityDefinition))
                .build();
    }

    private Process createTestProcess() {
        var processDefinition = createTestProcessDefinition();
        return Process.builder()
                .id(TEST_PROCESS_ID)
                .state(ProcessState.ACTIVE)
                .definition(processDefinition)
                .build();
    }

}