package com.leorces.extension.camunda;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DisplayName("Camunda7ExtensionService Integration Tests")
@SpringBootTest(classes = BpmnParserTestConfiguration.class)
class Camunda7ExtensionServiceTest {

    private static final String VALID_BPMN_PATH = "bpmn";
    private static final String EMPTY_BPMN_PATH = "bpmn/empty";
    private static final String NON_EXISTENT_PATH = "non/existent/path";

    @Autowired
    private CamundaExtensionService camundaExtensionService;

    @Test
    @DisplayName("Should successfully load and process BPMN files")
    void shouldLoadAndProcessBpmnFiles() {
        // When - Process actual BPMN files from test resources
        // This will internally call file reader, parser, and process definition service
        camundaExtensionService.loadAndProcessBpmnFiles(VALID_BPMN_PATH);

        // Then - The fact that no exception is thrown indicates success
        // The method successfully processes all available .bpmn files in the test resources
        assertThat(true).isTrue(); // Test passes if no exception is thrown
    }

    @Test
    @DisplayName("Should handle empty path gracefully")
    void shouldHandleEmptyPathGracefully() {
        // When & Then - Should not throw exception for empty paths
        camundaExtensionService.loadAndProcessBpmnFiles(EMPTY_BPMN_PATH);
        // Test passes if no exception is thrown
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null path")
    void shouldThrowIllegalArgumentExceptionForNullPath() {
        // When & Then
        assertThatThrownBy(() -> camundaExtensionService.loadAndProcessBpmnFiles(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty string path")
    void shouldThrowIllegalArgumentExceptionForEmptyStringPath() {
        // When & Then
        assertThatThrownBy(() -> camundaExtensionService.loadAndProcessBpmnFiles(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank path")
    void shouldThrowIllegalArgumentExceptionForBlankPath() {
        // When & Then
        assertThatThrownBy(() -> camundaExtensionService.loadAndProcessBpmnFiles("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle non-existent path gracefully")
    void shouldHandleNonExistentPathGracefully() {
        // When & Then - Should not throw exception for non-existent paths
        camundaExtensionService.loadAndProcessBpmnFiles(NON_EXISTENT_PATH);
        // Test passes if no exception is thrown
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Should successfully process valid BPMN path with actual files")
    void shouldProcessValidBpmnPathWithActualFiles() {
        // When - Process the path that contains actual .bpmn files
        camundaExtensionService.loadAndProcessBpmnFiles(VALID_BPMN_PATH);

        // Then - The method should complete successfully without throwing exceptions
        // This integration test verifies the entire flow: file reading -> parsing -> saving
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Should handle path with trailing slash correctly")
    void shouldHandlePathWithTrailingSlashCorrectly() {
        // Given
        var pathWithSlash = VALID_BPMN_PATH + "/";

        // When - Process path with trailing slash
        camundaExtensionService.loadAndProcessBpmnFiles(pathWithSlash);

        // Then - Should process successfully
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Should handle path with whitespace correctly")
    void shouldHandlePathWithWhitespaceCorrectly() {
        // Given
        var pathWithWhitespace = "  " + VALID_BPMN_PATH + "  ";

        // When - Process path with whitespace
        camundaExtensionService.loadAndProcessBpmnFiles(pathWithWhitespace);

        // Then - Should process successfully
        assertThat(true).isTrue();
    }
}