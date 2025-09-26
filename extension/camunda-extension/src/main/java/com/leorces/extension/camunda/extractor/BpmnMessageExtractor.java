package com.leorces.extension.camunda.extractor;


import org.springframework.stereotype.Component;
import org.w3c.dom.Element;


@Component
public class BpmnMessageExtractor {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    public Element findMessageDefinition(Element element) {
        var messageDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "messageEventDefinition");
        return messageDefinitions.getLength() > 0
                ? (Element) messageDefinitions.item(0)
                : null;
    }

    public String resolveMessageName(Element element, String messageRef) {
        if (messageRef == null || messageRef.isEmpty()) {
            return messageRef;
        }

        var document = element.getOwnerDocument();
        var messageElements = document.getElementsByTagName("bpmn:message");

        for (int i = 0; i < messageElements.getLength(); i++) {
            var messageElement = (Element) messageElements.item(i);
            if (messageRef.equals(messageElement.getAttribute("id"))) {
                return messageElement.getAttribute("name");
            }
        }

        return messageRef;
    }

}
