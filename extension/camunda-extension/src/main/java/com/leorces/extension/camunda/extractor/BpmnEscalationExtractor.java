package com.leorces.extension.camunda.extractor;

import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

@Component
public class BpmnEscalationExtractor {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    public Element findEscalationDefinition(Element element) {
        var escalationDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "escalationEventDefinition");
        return escalationDefinitions.getLength() > 0
                ? (Element) escalationDefinitions.item(0)
                : null;
    }

    public String getEscalationCode(Element escalationDefinition) {
        var errorRef = escalationDefinition.getAttribute("escalationRef");
        if (errorRef.isEmpty()) {
            return null;
        }

        // Find the root document to search for bpmn:escalation elements
        var document = escalationDefinition.getOwnerDocument();
        var errorElements = document.getElementsByTagNameNS(BPMN_NAMESPACE, "escalation");

        for (int i = 0; i < errorElements.getLength(); i++) {
            var errorElement = (Element) errorElements.item(i);
            if (errorRef.equals(errorElement.getAttribute("id"))) {
                return errorElement.getAttribute("escalationCode");
            }
        }

        return null;
    }

}
