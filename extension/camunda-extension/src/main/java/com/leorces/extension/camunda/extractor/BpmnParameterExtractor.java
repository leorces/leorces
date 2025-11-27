package com.leorces.extension.camunda.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.leorces.extension.camunda.BpmnConstants.*;

/**
 * Component responsible for extracting input and output parameters from BPMN elements.
 */
@Slf4j
@Component
public class BpmnParameterExtractor {

    /**
     * Extracts input parameters from a BPMN element.
     *
     * @param element the BPMN element to extract parameters from
     * @return map of input parameters
     */
    public Map<String, Object> extractInputParameters(Element element) {
        return extractParameters(element, INPUT_PARAMETER);
    }

    /**
     * Extracts output parameters from a BPMN element.
     *
     * @param element the BPMN element to extract parameters from
     * @return map of output parameters
     */
    public Map<String, Object> extractOutputParameters(Element element) {
        return extractParameters(element, OUTPUT_PARAMETER);
    }

    private Map<String, Object> extractParameters(Element element, String parameterType) {
        var parameters = new HashMap<String, Object>();
        var extensionElements = findExtensionElements(element);

        if (extensionElements != null) {
            extractParametersFromExtension(extensionElements, parameters, parameterType);
        }

        return parameters;
    }

    private Element findExtensionElements(Element element) {
        var extensionElements = element.getElementsByTagNameNS(BPMN_NAMESPACE, EXTENSION_ELEMENTS);
        return extensionElements.getLength() > 0 ? (Element) extensionElements.item(0) : null;
    }

    private void extractParametersFromExtension(Element extensionElement, Map<String, Object> parameters, String parameterType) {
        var inputOutputs = extensionElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, INPUT_OUTPUT);

        if (inputOutputs.getLength() > 0) {
            var inputOutput = (Element) inputOutputs.item(0);
            extractParametersFromInputOutput(inputOutput, parameters, parameterType);
        }
    }

    private void extractParametersFromInputOutput(Element inputOutput, Map<String, Object> parameters, String parameterType) {
        var parameterElements = inputOutput.getElementsByTagNameNS(CAMUNDA_NAMESPACE, parameterType);

        for (int i = 0; i < parameterElements.getLength(); i++) {
            var parameterElement = (Element) parameterElements.item(i);
            addParameter(parameterElement, parameters);
        }
    }

    private void addParameter(Element parameterElement, Map<String, Object> parameters) {
        var name = parameterElement.getAttribute(ATTRIBUTE_NAME);
        var value = parseParameterValue(parameterElement);
        parameters.put(name, value);
    }

    private Object parseParameterValue(Element parameterElement) {
        var lists = parameterElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, LIST);
        if (lists.getLength() > 0) {
            Element listEl = (Element) lists.item(0);
            return parseCamundaList(listEl);
        }

        var text = parameterElement.getTextContent();
        return text != null ? text.trim() : null;
    }

    private List<String> parseCamundaList(Element listElement) {
        var values = new ArrayList<String>();
        var valueNodes = listElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, VALUE);
        for (int i = 0; i < valueNodes.getLength(); i++) {
            var v = valueNodes.item(i).getTextContent();
            if (v != null) {
                values.add(v.trim());
            }
        }
        return values;
    }

}
