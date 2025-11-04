package com.leorces.extension.camunda;

import com.leorces.extension.camunda.exception.BpmnParseException;
import com.leorces.extension.camunda.extractor.BpmnDocumentParser;
import com.leorces.model.definition.ErrorItem;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.ProcessDefinitionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing BPMN files using DOM parser.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BpmnParser {

    private static final String CAMUNDA_ORIGIN = "Camunda";
    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final BpmnDocumentParser documentParser;
    private final BpmnActivityParser activityParser;

    /**
     * Parses a BPMN resource into ProcessDefinition.
     *
     * @param resource the BPMN resource to parse
     * @return parsed ProcessDefinition
     */
    public ProcessDefinition parse(Resource resource) {
        try {
            // Read BPMN content as string for metadata
            var bpmnContent = new String(resource.getInputStream().readAllBytes());
            // Parse document from the same content
            try (InputStream inputStream = resource.getInputStream()) {
                var document = documentParser.parseDocument(inputStream);
                return extractProcessDefinition(document, resource.getFilename(), bpmnContent);
            }
        } catch (Exception e) {
            var filename = resource.getFilename();
            log.error("Failed to parse BPMN file: {}", filename, e);
            throw new BpmnParseException(String.format("Failed to parse BPMN file: %s", filename), e);
        }
    }

    private ProcessDefinition extractProcessDefinition(Document document, String filename, String bpmnContent) {
        var processElement = documentParser.findProcessElement(document);
        var processId = processElement.getAttribute("id");
        var processName = processElement.getAttribute("name");

        // Activities directly in the main process should have null parentId (not subprocess)
        var activities = activityParser.extractActivities(processElement, null, processId);

        // Create metadata with schema, origin, and deployment
        var metadata = ProcessDefinitionMetadata.builder()
                .schema(bpmnContent)
                .origin(CAMUNDA_ORIGIN)
                .deployment(filename)
                .build();

        return ProcessDefinition.builder()
                .key(processId)
                .name(processName.isEmpty() ? processId : processName)
                .activities(activities)
                .messages(extractMessages(document))
                .errors(extractErrors(document))
                .metadata(metadata)
                .build();
    }

    private List<String> extractMessages(Document document) {
        var nodes = document.getElementsByTagNameNS(BPMN_NAMESPACE, "message");
        var result = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var element = (Element) nodes.item(i);
            result.add(element.getAttribute("name"));
        }
        return result;
    }

    private List<ErrorItem> extractErrors(Document document) {
        var nodes = document.getElementsByTagNameNS(BPMN_NAMESPACE, "error");
        var result = new ArrayList<ErrorItem>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var element = (Element) nodes.item(i);
            var errorItem = ErrorItem.builder()
                    .name(element.getAttribute("name"))
                    .errorCode(element.getAttribute("errorCode"))
                    .message(element.getAttribute("camunda:errorMessage"))
                    .build();
            result.add(errorItem);
        }
        return result;
    }

}