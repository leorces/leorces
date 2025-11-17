package com.leorces.extension.camunda.extractor.event;

import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
public class BpmnEscalationExtractor extends AbstractBpmnEventExtractor {

    public Element findEscalationDefinition(Element element) {
        return findEventDefinition(element);
    }

    public String getEscalationCode(Element escalationDefinition) {
        return getEventCode(escalationDefinition);
    }

    @Override
    protected String getEventDefinitionTagName() {
        return ESCALATION_EVENT_DEFINITION;
    }

    @Override
    protected String getEventTagName() {
        return ESCALATION;
    }

    @Override
    protected String getRefAttributeName() {
        return ATTRIBUTE_ESCALATION_REF;
    }

    @Override
    protected String getCodeAttributeName() {
        return ATTRIBUTE_ESCALATION_CODE;
    }

}
