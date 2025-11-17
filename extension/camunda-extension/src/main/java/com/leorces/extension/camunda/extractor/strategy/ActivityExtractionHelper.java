package com.leorces.extension.camunda.extractor.strategy;

import com.leorces.extension.camunda.extractor.*;
import com.leorces.extension.camunda.extractor.event.BpmnErrorExtractor;
import com.leorces.extension.camunda.extractor.event.BpmnEscalationExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
@RequiredArgsConstructor
public class ActivityExtractionHelper {

    private final BpmnElementExtractor elementExtractor;
    private final BpmnErrorExtractor errorExtractor;
    private final BpmnMessageExtractor messageExtractor;
    private final BpmnFlowExtractor flowExtractor;
    private final BpmnParameterExtractor parameterExtractor;
    private final BpmnConditionExtractor conditionExtractor;
    private final BpmnEscalationExtractor escalationExtractor;

    public <T> List<T> extractElements(Element processElement, String elementName, String parentId, String processId, BpmnElementExtractor.ElementCreator<T> creator) {
        return elementExtractor.extractElements(processElement, elementName, parentId, processId, creator);
    }

    public Element findMessageDefinition(Element element) {
        return messageExtractor.findMessageDefinition(element);
    }

    public Element findEscalationEventDefinition(Element element) {
        return escalationExtractor.findEscalationDefinition(element);
    }

    public String getMessageName(Element messageDefinition) {
        var messageRef = messageDefinition.getAttribute(ATTRIBUTE_MESSAGE_REF);
        return messageExtractor.resolveMessageName(messageDefinition, messageRef);
    }

    public Element findErrorDefinition(Element element) {
        return errorExtractor.findErrorDefinition(element);
    }

    public String getErrorCode(Element errorDefinition) {
        return errorExtractor.getErrorCode(errorDefinition);
    }

    public String getEscalationCode(Element escalationDefinition) {
        return escalationExtractor.getEscalationCode(escalationDefinition);
    }

    public List<String> extractIncoming(Element element) {
        return flowExtractor.extractIncoming(element);
    }

    public List<String> extractOutgoing(Element element) {
        return flowExtractor.extractOutgoing(element);
    }

    public List<Element> extractGatewayOutgoing(Element gateway) {
        return flowExtractor.extractGatewayOutgoing(gateway);
    }

    public Map<String, Object> extractInputParameters(Element element) {
        return parameterExtractor.extractInputParameters(element);
    }

    public Map<String, Object> extractOutputParameters(Element element) {
        return parameterExtractor.extractOutputParameters(element);
    }

    public String extractCondition(Element element) {
        return conditionExtractor.extractCondition(element);
    }

    public String getId(Element element) {
        return element.getAttribute(ATTRIBUTE_ID);
    }

    public String getName(Element element) {
        return element.getAttribute(ATTRIBUTE_NAME);
    }

}