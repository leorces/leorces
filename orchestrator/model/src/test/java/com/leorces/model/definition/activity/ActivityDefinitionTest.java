package com.leorces.model.definition.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActivityDefinition Interface Tests")
class ActivityDefinitionTest {

    @Test
    @DisplayName("Should define all required interface methods")
    void shouldDefineAllRequiredInterfaceMethods() {
        // Given
        var testImplementation = new TestActivityDefinitionImpl();

        // When & Then
        assertNotNull(testImplementation.id());
        assertNotNull(testImplementation.parentId());
        assertNotNull(testImplementation.name());
        assertNotNull(testImplementation.type());
        assertNotNull(testImplementation.incoming());
        assertNotNull(testImplementation.outgoing());
        assertNotNull(testImplementation.inputs());
        assertNotNull(testImplementation.outputs());
    }

    @Test
    @DisplayName("Should work with concrete implementation")
    void shouldWorkWithConcreteImplementation() {
        // Given
        var implementation = new TestActivityDefinitionImpl();

        // When
        var id = implementation.id();
        var parentId = implementation.parentId();
        var name = implementation.name();
        var type = implementation.type();
        var incoming = implementation.incoming();
        var outgoing = implementation.outgoing();
        var inputs = implementation.inputs();
        var outputs = implementation.outputs();

        // Then
        assertEquals("test-id", id);
        assertEquals("parent-id", parentId);
        assertEquals("Test Activity", name);
        assertEquals(ActivityType.EXTERNAL_TASK, type);
        assertEquals(List.of("incoming1", "incoming2"), incoming);
        assertEquals(List.of("outgoing1"), outgoing);
        assertEquals(Map.of("input1", "value1"), inputs);
        assertEquals(Map.of("output1", "result1"), outputs);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // Given
        var implementation = new ActivityDefinition() {
            @Override
            public String id() {
                return "test-id";
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
                return ActivityType.START_EVENT;
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

        // When & Then
        assertEquals("test-id", implementation.id());
        assertNull(implementation.parentId());
        assertEquals("Test Activity", implementation.name());
        assertEquals(ActivityType.START_EVENT, implementation.type());
        assertTrue(implementation.incoming().isEmpty());
        assertTrue(implementation.outgoing().isEmpty());
        assertTrue(implementation.inputs().isEmpty());
        assertTrue(implementation.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should support different activity types")
    void shouldSupportDifferentActivityTypes() {
        // Given
        var externalTask = createImplementationWithType(ActivityType.EXTERNAL_TASK);
        var startEvent = createImplementationWithType(ActivityType.START_EVENT);
        var endEvent = createImplementationWithType(ActivityType.END_EVENT);
        var parallelGateway = createImplementationWithType(ActivityType.PARALLEL_GATEWAY);

        // When & Then
        assertEquals(ActivityType.EXTERNAL_TASK, externalTask.type());
        assertEquals(ActivityType.START_EVENT, startEvent.type());
        assertEquals(ActivityType.END_EVENT, endEvent.type());
        assertEquals(ActivityType.PARALLEL_GATEWAY, parallelGateway.type());
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        // Given
        var implementation = new ActivityDefinition() {
            @Override
            public String id() {
                return "empty-collections-test";
            }

            @Override
            public String parentId() {
                return null;
            }

            @Override
            public String name() {
                return "Empty Collections Test";
            }

            @Override
            public ActivityType type() {
                return ActivityType.END_EVENT;
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

        // When & Then
        assertNotNull(implementation.incoming());
        assertNotNull(implementation.outgoing());
        assertNotNull(implementation.inputs());
        assertNotNull(implementation.outputs());
        assertTrue(implementation.incoming().isEmpty());
        assertTrue(implementation.outgoing().isEmpty());
        assertTrue(implementation.inputs().isEmpty());
        assertTrue(implementation.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle complex inputs and outputs")
    void shouldHandleComplexInputsAndOutputs() {
        // Given
        var complexInputs = Map.of(
                "stringInput", "test",
                "numberInput", 42,
                "booleanInput", true,
                "listInput", List.of("item1", "item2")
        );
        var complexOutputs = Map.of(
                "result", "success",
                "count", 10,
                "data", Map.of("nested", "value")
        );

        var implementation = new ActivityDefinition() {
            @Override
            public String id() {
                return "complex-test";
            }

            @Override
            public String parentId() {
                return "parent";
            }

            @Override
            public String name() {
                return "Complex Test";
            }

            @Override
            public ActivityType type() {
                return ActivityType.RECEIVE_TASK;
            }

            @Override
            public List<String> incoming() {
                return List.of("in1", "in2", "in3");
            }

            @Override
            public List<String> outgoing() {
                return List.of("out1", "out2");
            }

            @Override
            public Map<String, Object> inputs() {
                return complexInputs;
            }

            @Override
            public Map<String, Object> outputs() {
                return complexOutputs;
            }
        };

        // When & Then
        assertEquals(complexInputs, implementation.inputs());
        assertEquals(complexOutputs, implementation.outputs());
        assertEquals(4, implementation.inputs().size());
        assertEquals(3, implementation.outputs().size());
        assertEquals("test", implementation.inputs().get("stringInput"));
        assertEquals(42, implementation.inputs().get("numberInput"));
        assertEquals("success", implementation.outputs().get("result"));
    }

    @Test
    @DisplayName("Should handle hierarchical relationships")
    void shouldHandleHierarchicalRelationships() {
        // Given
        var parentImplementation = new ActivityDefinition() {
            @Override
            public String id() {
                return "parent-activity";
            }

            @Override
            public String parentId() {
                return null;
            }

            @Override
            public String name() {
                return "Parent Activity";
            }

            @Override
            public ActivityType type() {
                return ActivityType.SUBPROCESS;
            }

            @Override
            public List<String> incoming() {
                return List.of("start");
            }

            @Override
            public List<String> outgoing() {
                return List.of("end");
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

        var childImplementation = new ActivityDefinition() {
            @Override
            public String id() {
                return "child-activity";
            }

            @Override
            public String parentId() {
                return "parent-activity";
            }

            @Override
            public String name() {
                return "Child Activity";
            }

            @Override
            public ActivityType type() {
                return ActivityType.EXTERNAL_TASK;
            }

            @Override
            public List<String> incoming() {
                return List.of("parent-start");
            }

            @Override
            public List<String> outgoing() {
                return List.of("parent-end");
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

        // When & Then
        assertNull(parentImplementation.parentId());
        assertEquals("parent-activity", childImplementation.parentId());
        assertEquals(ActivityType.SUBPROCESS, parentImplementation.type());
        assertEquals(ActivityType.EXTERNAL_TASK, childImplementation.type());
    }

    private ActivityDefinition createImplementationWithType(ActivityType type) {
        return new ActivityDefinition() {
            @Override
            public String id() {
                return "id-" + type.name().toLowerCase();
            }

            @Override
            public String parentId() {
                return null;
            }

            @Override
            public String name() {
                return type.name() + " Activity";
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

    private static class TestActivityDefinitionImpl implements ActivityDefinition {
        @Override
        public String id() {
            return "test-id";
        }

        @Override
        public String parentId() {
            return "parent-id";
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
            return List.of("incoming1", "incoming2");
        }

        @Override
        public List<String> outgoing() {
            return List.of("outgoing1");
        }

        @Override
        public Map<String, Object> inputs() {
            return Map.of("input1", "value1");
        }

        @Override
        public Map<String, Object> outputs() {
            return Map.of("output1", "result1");
        }

    }

}