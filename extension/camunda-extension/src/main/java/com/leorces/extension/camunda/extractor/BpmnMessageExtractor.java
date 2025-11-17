package com.leorces.extension.camunda.extractor;

import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
public class BpmnMessageExtractor {

    public Element findMessageDefinition(Element element) {
        var messageDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, MESSAGE_EVENT_DEFINITION);
        return messageDefinitions.getLength() > 0
                ? (Element) messageDefinitions.item(0)
                : null;
    }

    public String resolveMessageName(Element element, String messageRef) {
        if (messageRef == null || messageRef.isEmpty()) {
            return messageRef;
        }

        var document = element.getOwnerDocument();
        var messageElements = document.getElementsByTagName(BPMN_MESSAGE);

        for (int i = 0; i < messageElements.getLength(); i++) {
            var messageElement = (Element) messageElements.item(i);
            if (messageRef.equals(messageElement.getAttribute(ATTRIBUTE_ID))) {
                return messageElement.getAttribute(ATTRIBUTE_NAME);
            }
        }

        return messageRef;
    }

}
