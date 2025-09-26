package com.leorces.extension.camunda.extractor.strategy;


import com.leorces.extension.camunda.extractor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class ActivityExtractionHelper {

    private final BpmnElementExtractor elementExtractor;
    private final BpmnErrorExtractor errorExtractor;
    private final BpmnMessageExtractor messageExtractor;
    private final BpmnFlowExtractor flowExtractor;
    private final BpmnParameterExtractor parameterExtractor;
    private final BpmnConditionExtractor conditionExtractor;

    public <T> List<T> extractElements(Element processElement, String elementName, String parentId, String processId, BpmnElementExtractor.ElementCreator<T> creator) {
        return elementExtractor.extractElements(processElement, elementName, parentId, processId, creator);
    }

    public Element findMessageDefinition(Element element) {
        return messageExtractor.findMessageDefinition(element);
    }

    public String getMessageName(Element messageDefinition) {
        var messageRef = messageDefinition.getAttribute("messageRef");
        return resolveMessageName(messageDefinition, messageRef);
    }

    public String resolveMessageName(Element element, String messageRef) {
        return messageExtractor.resolveMessageName(element, messageRef);
    }

    public Element findErrorDefinition(Element element) {
        return errorExtractor.findErrorDefinition(element);
    }

    public String getErrorCode(Element errorDefinition) {
        return errorExtractor.getErrorCode(errorDefinition);
    }

    public List<String> extractIncoming(Element element) {
        return flowExtractor.extractIncoming(element);
    }

    public List<String> extractOutgoing(Element element) {
        return flowExtractor.extractOutgoing(element);
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
        return element.getAttribute("id");
    }

    public String getName(Element element) {
        return element.getAttribute("name");
    }

}