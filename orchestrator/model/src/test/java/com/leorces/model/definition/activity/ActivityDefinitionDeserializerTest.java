package com.leorces.model.definition.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.leorces.model.definition.activity.event.*;
import com.leorces.model.definition.activity.gateway.EventBasedGateway;
import com.leorces.model.definition.activity.gateway.ExclusiveGateway;
import com.leorces.model.definition.activity.gateway.InclusiveGateway;
import com.leorces.model.definition.activity.gateway.ParallelGateway;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.definition.activity.subprocess.EventSubprocess;
import com.leorces.model.definition.activity.subprocess.Subprocess;
import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.definition.activity.task.ReceiveTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActivityDefinitionDeserializer Tests")
class ActivityDefinitionDeserializerTest {

    private static final String TEST_ID = "test-id";
    private static final String TEST_NAME = "Test Activity";

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        var module = new SimpleModule();
        module.addDeserializer(ActivityDefinition.class, new ActivityDefinitionDeserializer());
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
    }

    @Test
    @DisplayName("Should deserialize START_EVENT type")
    void shouldDeserializeStartEvent() throws Exception {
        // Given
        var json = """
                {
                    "type": "START_EVENT",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(StartEvent.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize END_EVENT type")
    void shouldDeserializeEndEvent() throws Exception {
        // Given
        var json = """
                {
                    "type": "END_EVENT",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(EndEvent.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize EXTERNAL_TASK type")
    void shouldDeserializeExternalTask() throws Exception {
        // Given
        var json = """
                {
                    "type": "EXTERNAL_TASK",
                    "id": "%s",
                    "name": "%s",
                    "topic": "test-topic"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ExternalTask.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize PARALLEL_GATEWAY type")
    void shouldDeserializeParallelGateway() throws Exception {
        // Given
        var json = """
                {
                    "type": "PARALLEL_GATEWAY",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ParallelGateway.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize EXCLUSIVE_GATEWAY type")
    void shouldDeserializeExclusiveGateway() throws Exception {
        // Given
        var json = """
                {
                    "type": "EXCLUSIVE_GATEWAY",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ExclusiveGateway.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize INCLUSIVE_GATEWAY type")
    void shouldDeserializeInclusiveGateway() throws Exception {
        // Given
        var json = """
                {
                    "type": "INCLUSIVE_GATEWAY",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(InclusiveGateway.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize ERROR_END_EVENT type")
    void shouldDeserializeErrorEndEvent() throws Exception {
        // Given
        var json = """
                {
                    "type": "ERROR_END_EVENT",
                    "id": "%s",
                    "name": "%s",
                    "errorCode": "E001"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ErrorEndEvent.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize SUBPROCESS type")
    void shouldDeserializeSubprocess() throws Exception {
        // Given
        var json = """
                {
                    "type": "SUBPROCESS",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(Subprocess.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should deserialize CALL_ACTIVITY type")
    void shouldDeserializeCallActivity() throws Exception {
        // Given
        var json = """
                {
                    "type": "CALL_ACTIVITY",
                    "id": "%s",
                    "name": "%s",
                    "calledElement": "called-process"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When
        var result = objectMapper.readValue(json, ActivityDefinition.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(CallActivity.class, result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_NAME, result.name());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when type field is missing")
    void shouldThrowExceptionWhenTypeFieldMissing() {
        // Given
        var json = """
                {
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () -> 
            objectMapper.readValue(json, ActivityDefinition.class));
        assertEquals("Missing 'type' field in activity definition", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when type is invalid")
    void shouldThrowExceptionWhenTypeIsInvalid() {
        // Given
        var json = """
                {
                    "type": "INVALID_TYPE",
                    "id": "%s",
                    "name": "%s"
                }
                """.formatted(TEST_ID, TEST_NAME);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            objectMapper.readValue(json, ActivityDefinition.class));
    }
}