package com.leorces.extension.camunda.extractor.strategy.subprocess;


import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class CallActivityExtractor implements ActivityExtractionStrategy {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String CAMUNDA_NAMESPACE = "http://camunda.org/schema/1.0/bpmn";

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "callActivity", parentId, processId, this::createCallActivity);
    }

    private CallActivity createCallActivity(Element element, String parentId, String processId) {
        var inputMappings = extractInputMappings(element);
        var outputMappings = extractOutputMappings(element);

        return CallActivity.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .calledElement(element.getAttribute("calledElement"))
                .calledElementVersion(getCalledElementVersion(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .inputMappings(inputMappings)
                .outputMappings(outputMappings)
                .build();
    }

    private Integer getCalledElementVersion(Element element) {
        var version = element.getAttributeNS(CAMUNDA_NAMESPACE, "calledElementVersion");
        return !version.isBlank() ? Integer.parseInt(version) : null;
    }

    private List<VariableMapping> extractInputMappings(Element element) {
        return extractVariableMappings(element, "in");
    }

    private List<VariableMapping> extractOutputMappings(Element element) {
        return extractVariableMappings(element, "out");
    }

    private List<VariableMapping> extractVariableMappings(Element element, String mappingType) {
        var extensionElement = getExtensionElement(element);
        if (extensionElement == null) {
            return new ArrayList<>();
        }

        var mappings = new ArrayList<VariableMapping>();
        var mappingElements = extensionElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, mappingType);

        for (int i = 0; i < mappingElements.getLength(); i++) {
            var mapping = (Element) mappingElements.item(i);
            mappings.add(createVariableMapping(mapping));
        }

        return mappings;
    }

    private Element getExtensionElement(Element element) {
        var extensionElements = element.getElementsByTagNameNS(BPMN_NAMESPACE, "extensionElements");
        return extensionElements.getLength() > 0 ? (Element) extensionElements.item(0) : null;
    }

    private VariableMapping createVariableMapping(Element mapping) {
        return VariableMapping.builder()
                .source(getStringOrNull(mapping.getAttribute("source")))
                .target(getStringOrNull(mapping.getAttribute("target")))
                .sourceExpression(getStringOrNull(mapping.getAttribute("sourceExpression")))
                .variables(getStringOrNull(mapping.getAttribute("variables")))
                .build();
    }

    private String getStringOrNull(String value) {
        return value.isEmpty() ? null : value;
    }

}