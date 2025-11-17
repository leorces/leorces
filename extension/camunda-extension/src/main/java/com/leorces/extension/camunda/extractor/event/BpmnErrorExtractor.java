package com.leorces.extension.camunda.extractor.event;

import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
public class BpmnErrorExtractor extends AbstractBpmnEventExtractor {

    public Element findErrorDefinition(Element element) {
        return findEventDefinition(element);
    }

    public String getErrorCode(Element errorDefinition) {
        return getEventCode(errorDefinition);
    }

    @Override
    protected String getEventDefinitionTagName() {
        return ERROR_EVENT_DEFINITION;
    }

    @Override
    protected String getEventTagName() {
        return ERROR;
    }

    @Override
    protected String getRefAttributeName() {
        return ATTRIBUTE_ERROR_REF;
    }

    @Override
    protected String getCodeAttributeName() {
        return ATTRIBUTE_ERROR_CODE;
    }

}
