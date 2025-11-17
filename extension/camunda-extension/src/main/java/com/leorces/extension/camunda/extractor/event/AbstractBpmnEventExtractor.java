package com.leorces.extension.camunda.extractor.event;

import org.w3c.dom.Element;

import static com.leorces.extension.camunda.BpmnConstants.ATTRIBUTE_ID;
import static com.leorces.extension.camunda.BpmnConstants.BPMN_NAMESPACE;

/**
 * Abstract base class for extracting BPMN event definitions (error, escalation, etc.).
 */
public abstract class AbstractBpmnEventExtractor {

    /**
     * Finds event definition element within the given element.
     *
     * @param element the BPMN element to search in
     * @return the event definition element or null if not found
     */
    public Element findEventDefinition(Element element) {
        var eventDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, getEventDefinitionTagName());
        return eventDefinitions.getLength() > 0
                ? (Element) eventDefinitions.item(0)
                : null;
    }

    /**
     * Extracts the event code from the event definition.
     *
     * @param eventDefinition the event definition element
     * @return the event code or null if not found
     */
    public String getEventCode(Element eventDefinition) {
        var eventRef = extractEventRef(eventDefinition);
        if (eventRef.isEmpty()) {
            return null;
        }
        return findEventCodeByRef(eventDefinition, eventRef);
    }

    private String extractEventRef(Element eventDefinition) {
        return eventDefinition.getAttribute(getRefAttributeName());
    }

    private String findEventCodeByRef(Element eventDefinition, String eventRef) {
        var document = eventDefinition.getOwnerDocument();
        var eventElements = document.getElementsByTagNameNS(BPMN_NAMESPACE, getEventTagName());

        for (int i = 0; i < eventElements.getLength(); i++) {
            var eventElement = (Element) eventElements.item(i);
            if (isMatchingEvent(eventElement, eventRef)) {
                return eventElement.getAttribute(getCodeAttributeName());
            }
        }
        return null;
    }

    private boolean isMatchingEvent(Element eventElement, String eventRef) {
        return eventRef.equals(eventElement.getAttribute(ATTRIBUTE_ID));
    }

    /**
     * Returns the tag name for the event definition (e.g., "errorEventDefinition").
     */
    protected abstract String getEventDefinitionTagName();

    /**
     * Returns the tag name for the event element (e.g., "error").
     */
    protected abstract String getEventTagName();

    /**
     * Returns the attribute name for the event reference (e.g., "errorRef").
     */
    protected abstract String getRefAttributeName();

    /**
     * Returns the attribute name for the event code (e.g., "errorCode").
     */
    protected abstract String getCodeAttributeName();

}
