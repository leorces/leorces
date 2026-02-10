package com.leorces.extension.camunda.extractor.strategy.subprocess;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.definition.attribute.MultiInstanceLoopCharacteristics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
@RequiredArgsConstructor
public class CallActivityExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(
                processElement,
                CALL_ACTIVITY,
                parentId,
                processId,
                this::createCallActivity
        );
    }

    private CallActivity createCallActivity(Element element, String parentId, String processId) {
        return CallActivity.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .calledElement(element.getAttribute(ATTRIBUTE_CALLED_ELEMENT))
                .calledElementVersion(getCalledElementVersion(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .inputMappings(extractInputMappings(element))
                .outputMappings(extractOutputMappings(element))
                .multiInstanceLoopCharacteristics(extractMultiInstanceLoopCharacteristics(element))
                .build();
    }

    private Integer getCalledElementVersion(Element element) {
        var version = element.getAttributeNS(CAMUNDA_NAMESPACE, ATTRIBUTE_CALLED_ELEMENT_VERSION);
        return !version.isBlank() ? Integer.parseInt(version) : null;
    }

    private List<VariableMapping> extractInputMappings(Element element) {
        return extractVariableMappings(element, MAPPING_IN);
    }

    private List<VariableMapping> extractOutputMappings(Element element) {
        return extractVariableMappings(element, MAPPING_OUT);
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

    private MultiInstanceLoopCharacteristics extractMultiInstanceLoopCharacteristics(Element element) {
        var loopCharacteristicsElements = element.getElementsByTagNameNS(BPMN_NAMESPACE, ATTRIBUTE_MULTI_INSTANCE_LOOP_CHARACTERISTICS);
        if (loopCharacteristicsElements.getLength() == 0) {
            return null;
        }

        var loopCharacteristics = (Element) loopCharacteristicsElements.item(0);
        return MultiInstanceLoopCharacteristics.builder()
                .collection(getStringOrNull(loopCharacteristics.getAttributeNS(CAMUNDA_NAMESPACE, ATTRIBUTE_COLLECTION)))
                .elementVariable(getStringOrNull(loopCharacteristics.getAttributeNS(CAMUNDA_NAMESPACE, ATTRIBUTE_ELEMENT_VARIABLE)))
                .isSequential(Boolean.parseBoolean(loopCharacteristics.getAttribute(ATTRIBUTE_IS_SEQUENTIAL)))
                .build();
    }

    private Element getExtensionElement(Element element) {
        var extensionElements = element.getElementsByTagNameNS(BPMN_NAMESPACE, EXTENSION_ELEMENTS);
        return extensionElements.getLength() > 0 ? (Element) extensionElements.item(0) : null;
    }

    private VariableMapping createVariableMapping(Element mapping) {
        return VariableMapping.builder()
                .source(getStringOrNull(mapping.getAttribute(ATTRIBUTE_SOURCE)))
                .target(getStringOrNull(mapping.getAttribute(ATTRIBUTE_TARGET)))
                .sourceExpression(getStringOrNull(mapping.getAttribute(ATTRIBUTE_SOURCE_EXPRESSION)))
                .variables(getStringOrNull(mapping.getAttribute(ATTRIBUTE_VARIABLES)))
                .build();
    }

    private String getStringOrNull(String value) {
        return value.isEmpty() ? null : value;
    }

}