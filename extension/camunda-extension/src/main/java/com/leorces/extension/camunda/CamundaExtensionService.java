package com.leorces.extension.camunda;


import com.leorces.api.DefinitionService;
import com.leorces.model.definition.ProcessDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Main service that orchestrates BPMN parsing and saving process definitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CamundaExtensionService {

    private final BpmnFileReader bpmnFileReader;
    private final BpmnParser bpmnParser;
    private final DefinitionService definitionService;

    /**
     * Loads and processes all BPMN files from the specified path.
     *
     * @param bpmnPath the path to search for BPMN files
     */
    public void loadAndProcessBpmnFiles(String bpmnPath) {
        log.info("Starting BPMN processing from path: {}", bpmnPath);

        var bpmnFiles = loadBpmnFiles(bpmnPath);

        if (bpmnFiles.isEmpty()) {
            log.info("No BPMN files to process. Skipping processing.");
            return;
        }

        var processDefinitions = parseBpmnFiles(bpmnFiles);
        var savedDefinitions = saveProcessDefinitions(processDefinitions);

        log.info("Successfully processed {} BPMN files", savedDefinitions.size());
    }

    private List<Resource> loadBpmnFiles(String bpmnPath) {
        return bpmnFileReader.readBpmnFiles(bpmnPath);
    }

    private List<ProcessDefinition> parseBpmnFiles(List<Resource> bpmnFiles) {
        return bpmnFiles.stream()
                .map(this::parseResource)
                .toList();
    }

    private ProcessDefinition parseResource(Resource resource) {
        log.debug("Parsing BPMN file: {}", resource.getFilename());
        return bpmnParser.parse(resource);
    }

    private List<ProcessDefinition> saveProcessDefinitions(List<ProcessDefinition> processDefinitions) {
        log.info("Saving {} process definitions", processDefinitions.size());
        return definitionService.save(processDefinitions);
    }

}