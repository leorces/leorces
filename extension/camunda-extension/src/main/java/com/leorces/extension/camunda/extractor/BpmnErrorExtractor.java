package com.leorces.extension.camunda.extractor;


import org.springframework.stereotype.Component;
import org.w3c.dom.Element;


@Component
public class BpmnErrorExtractor {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    public Element findErrorDefinition(Element element) {
        var errorDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "errorEventDefinition");
        return errorDefinitions.getLength() > 0 ? (Element) errorDefinitions.item(0) : null;
    }

    public String getErrorCode(Element errorDefinition) {
        var errorRef = errorDefinition.getAttribute("errorRef");
        if (errorRef.isEmpty()) {
            return null;
        }

        // Find the root document to search for bpmn:error elements
        var document = errorDefinition.getOwnerDocument();
        var errorElements = document.getElementsByTagNameNS(BPMN_NAMESPACE, "error");

        for (int i = 0; i < errorElements.getLength(); i++) {
            var errorElement = (Element) errorElements.item(i);
            if (errorRef.equals(errorElement.getAttribute("id"))) {
                return errorElement.getAttribute("errorCode");
            }
        }

        return null;
    }
}
