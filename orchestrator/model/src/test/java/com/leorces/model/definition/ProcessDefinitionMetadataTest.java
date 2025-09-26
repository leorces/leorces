package com.leorces.model.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Process Definition Metadata Tests")
class ProcessDefinitionMetadataTest {

    private static final String TEST_SCHEMA = "testSchema";
    private static final String TEST_ORIGIN = "testOrigin";
    private static final String TEST_DEPLOYMENT = "testDeployment";

    @Test
    @DisplayName("Should create ProcessDefinitionMetadata with all fields using builder")
    void shouldCreateProcessDefinitionMetadataWithAllFields() {
        // When
        var metadata = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        // Then
        assertNotNull(metadata);
        assertEquals(TEST_SCHEMA, metadata.schema());
        assertEquals(TEST_ORIGIN, metadata.origin());
        assertEquals(TEST_DEPLOYMENT, metadata.deployment());
    }

    @Test
    @DisplayName("Should create ProcessDefinitionMetadata with null fields")
    void shouldCreateProcessDefinitionMetadataWithNullFields() {
        // When
        var metadata = ProcessDefinitionMetadata.builder()
                .schema(null)
                .origin(null)
                .deployment(null)
                .build();

        // Then
        assertNotNull(metadata);
        assertNull(metadata.schema());
        assertNull(metadata.origin());
        assertNull(metadata.deployment());
    }

    @Test
    @DisplayName("Should create ProcessDefinitionMetadata with minimal fields")
    void shouldCreateProcessDefinitionMetadataWithMinimalFields() {
        // When
        var metadata = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .build();

        // Then
        assertNotNull(metadata);
        assertEquals(TEST_SCHEMA, metadata.schema());
        assertNull(metadata.origin());
        assertNull(metadata.deployment());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var metadata1 = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        var metadata2 = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        var metadata3 = ProcessDefinitionMetadata.builder()
                .schema("differentSchema")
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        // When & Then
        assertEquals(metadata1, metadata2);
        assertNotEquals(metadata1, metadata3);
        assertNotEquals(null, metadata1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var metadata1 = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        var metadata2 = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        // When & Then
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var metadata = ProcessDefinitionMetadata.builder()
                .schema(TEST_SCHEMA)
                .origin(TEST_ORIGIN)
                .deployment(TEST_DEPLOYMENT)
                .build();

        // When
        var toStringResult = metadata.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ProcessDefinitionMetadata"));
        assertTrue(toStringResult.contains(TEST_SCHEMA));
        assertTrue(toStringResult.contains(TEST_ORIGIN));
        assertTrue(toStringResult.contains(TEST_DEPLOYMENT));
    }

    @Test
    @DisplayName("Should handle empty strings correctly")
    void shouldHandleEmptyStringsCorrectly() {
        // Given
        var emptySchema = "";
        var emptyOrigin = "";
        var emptyDeployment = "";

        // When
        var metadata = ProcessDefinitionMetadata.builder()
                .schema(emptySchema)
                .origin(emptyOrigin)
                .deployment(emptyDeployment)
                .build();

        // Then
        assertNotNull(metadata);
        assertEquals(emptySchema, metadata.schema());
        assertEquals(emptyOrigin, metadata.origin());
        assertEquals(emptyDeployment, metadata.deployment());
    }

    @Test
    @DisplayName("Should handle typical BPMN schema values")
    void shouldHandleTypicalBpmnSchemaValues() {
        // Given
        var bpmnSchema1 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
        var bpmnSchema2 = "http://bpmn.io/schema/bpmn";

        // When
        var metadata1 = ProcessDefinitionMetadata.builder()
                .schema(bpmnSchema1)
                .build();

        var metadata2 = ProcessDefinitionMetadata.builder()
                .schema(bpmnSchema2)
                .build();

        // Then
        assertNotNull(metadata1);
        assertNotNull(metadata2);
        assertEquals(bpmnSchema1, metadata1.schema());
        assertEquals(bpmnSchema2, metadata2.schema());
        assertNotEquals(metadata1, metadata2);
    }

}