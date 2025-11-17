package com.leorces.extension.camunda.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Slf4j
@Component
public class BpmnConditionExtractor {

    public String extractCondition(Element element) {
        var conditionalEventDefinition = element.getElementsByTagNameNS(BPMN_NAMESPACE, CONDITIONAL_EVENT_DEFINITION);
        if (conditionalEventDefinition.getLength() <= 0) {
            return EMPTY_STRING;
        }

        var conditionalDef = (Element) conditionalEventDefinition.item(0);
        var conditions = conditionalDef.getElementsByTagNameNS(BPMN_NAMESPACE, CONDITION);

        if (conditions.getLength() <= 0) {
            return EMPTY_STRING;
        }

        var conditionElem = (Element) conditions.item(0);
        return conditionElem.getTextContent().trim();
    }

    public String extractSequenceFlowCondition(Element sequenceFlow) {
        var expression = sequenceFlow.getElementsByTagNameNS(BPMN_NAMESPACE, CONDITION_EXPRESSION);
        if (expression.getLength() == 0) {
            return EMPTY_STRING;
        }
        var expr = (Element) expression.item(0);
        return expr.getTextContent().trim();
    }

}
