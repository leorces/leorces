package com.leorces.extension.camunda;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DisplayName("BpmnFileReader Integration Tests")
@SpringBootTest(classes = BpmnParserTestConfiguration.class)
class BpmnFileReaderTest {

    private static final String VALID_BPMN_PATH = "bpmn";
    private static final String EMPTY_BPMN_PATH = "bpmn/empty";
    private static final String NON_EXISTENT_PATH = "non/existent/path";

    @Autowired
    private BpmnFileReader bpmnFileReader;

    @Test
    @DisplayName("Should successfully read BPMN files from valid path")
    void shouldReadBpmnFilesFromValidPath() {
        // When
        var actualResources = bpmnFileReader.readBpmnFiles(VALID_BPMN_PATH);

        // Then
        assertThat(actualResources).isNotNull();
        assertThat(actualResources).isNotEmpty();

        // Verify that actual BPMN files are found in test resources
        actualResources.forEach(resource -> {
            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.getFilename()).endsWith(".bpmn");
        });
    }

    @Test
    @DisplayName("Should return empty list when no BPMN files found")
    void shouldReturnEmptyListWhenNoBpmnFilesFound() {
        // When
        var actualResources = bpmnFileReader.readBpmnFiles(EMPTY_BPMN_PATH);

        // Then
        assertThat(actualResources).isNotNull();
        assertThat(actualResources).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when path does not exist")
    void shouldReturnEmptyListWhenPathDoesNotExist() {
        // When
        var actualResources = bpmnFileReader.readBpmnFiles(NON_EXISTENT_PATH);

        // Then
        assertThat(actualResources).isNotNull();
        assertThat(actualResources).isEmpty();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null path")
    void shouldThrowIllegalArgumentExceptionForNullPath() {
        // When & Then
        assertThatThrownBy(() -> bpmnFileReader.readBpmnFiles(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BPMN path cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty path")
    void shouldThrowIllegalArgumentExceptionForEmptyPath() {
        // When & Then
        assertThatThrownBy(() -> bpmnFileReader.readBpmnFiles(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BPMN path cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank path")
    void shouldThrowIllegalArgumentExceptionForBlankPath() {
        // When & Then
        assertThatThrownBy(() -> bpmnFileReader.readBpmnFiles("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BPMN path cannot be null or empty");
    }

    @Test
    @DisplayName("Should handle path with trailing slash correctly")
    void shouldHandlePathWithTrailingSlashCorrectly() {
        // Given
        var pathWithSlash = "bpmn/";

        // When
        var actualResources = bpmnFileReader.readBpmnFiles(pathWithSlash);

        // Then
        assertThat(actualResources).isNotNull();
        // Should find the same files as without trailing slash
        var resourcesWithoutSlash = bpmnFileReader.readBpmnFiles("bpmn");
        assertThat(actualResources).hasSize(resourcesWithoutSlash.size());
    }

    @Test
    @DisplayName("Should handle path with whitespace correctly")
    void shouldHandlePathWithWhitespaceCorrectly() {
        // Given
        var pathWithWhitespace = "  bpmn  ";

        // When
        var actualResources = bpmnFileReader.readBpmnFiles(pathWithWhitespace);

        // Then
        assertThat(actualResources).isNotNull();
        // Should find the same files as the trimmed path
        var normalResources = bpmnFileReader.readBpmnFiles("bpmn");
        assertThat(actualResources).hasSize(normalResources.size());
    }

    @Test
    @DisplayName("Should handle specific BPMN files correctly")
    void shouldHandleSpecificBpmnFilesCorrectly() {
        // When
        var actualResources = bpmnFileReader.readBpmnFiles(VALID_BPMN_PATH);

        // Then
        assertThat(actualResources).isNotNull();
        assertThat(actualResources).isNotEmpty();

        // Verify that we can find expected BPMN files
        var filenames = actualResources.stream()
                .map(Resource::getFilename)
                .toList();

        assertThat(filenames).anyMatch(name -> name != null && name.contains(".bpmn"));
    }

}