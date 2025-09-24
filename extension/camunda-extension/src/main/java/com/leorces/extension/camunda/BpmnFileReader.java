package com.leorces.extension.camunda;


import com.leorces.extension.camunda.exception.BpmnFileReadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


/**
 * Service for reading BPMN files from a configured path.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BpmnFileReader {

    private static final String BPMN_FILE_PATTERN = "**/*.bpmn";

    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Reads all BPMN files from the specified path.
     *
     * @param bpmnPath the path to search for BPMN files
     * @return list of BPMN file resources
     */
    public List<Resource> readBpmnFiles(String bpmnPath) {
        try {
            var searchPattern = buildSearchPattern(bpmnPath);
            var resources = resourcePatternResolver.getResources(searchPattern);

            if (resources.length == 0) {
                log.info("No BPMN files found in path: {}", bpmnPath);
                return List.of();
            }

            log.info("Found {} BPMN files in path: {}", resources.length, bpmnPath);
            return List.of(resources);
        } catch (FileNotFoundException e) {
            log.info("BPMN path does not exist: {}. No BPMN files to process.", bpmnPath);
            return List.of();
        } catch (IOException e) {
            log.error("Failed to read BPMN files from path: {}", bpmnPath, e);
            throw new BpmnFileReadException(String.format("Failed to read BPMN files from path: %s", bpmnPath), e);
        }
    }

    private String buildSearchPattern(String bpmnPath) {
        var normalizedPath = normalizePath(bpmnPath);

        // Add a classpath prefix if no protocol is specified
        if (!normalizedPath.contains(":")) {
            normalizedPath = "classpath*:" + normalizedPath;
        }

        return normalizedPath + "/" + BPMN_FILE_PATTERN;
    }

    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("BPMN path cannot be null or empty");
        }

        var trimmedPath = path.trim();
        if (trimmedPath.endsWith("/")) {
            return trimmedPath.substring(0, trimmedPath.length() - 1);
        }
        return trimmedPath;
    }

}