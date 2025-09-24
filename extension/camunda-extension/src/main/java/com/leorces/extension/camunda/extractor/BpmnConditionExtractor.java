package com.leorces.extension.camunda.extractor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;


@Slf4j
@Component
public class BpmnConditionExtractor {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    public String extractCondition(Element element) {
        var conditionExpression = "";
        var conditionalEventDefinition = element.getElementsByTagNameNS(BPMN_NAMESPACE, "conditionalEventDefinition");
        if (conditionalEventDefinition.getLength() > 0) {
            var conditionalDef = (Element) conditionalEventDefinition.item(0);
            var conditions = conditionalDef.getElementsByTagNameNS(BPMN_NAMESPACE, "condition");
            if (conditions.getLength() > 0) {
                var conditionElem = (Element) conditions.item(0);
                conditionExpression = conditionElem.getTextContent().trim();
            }
        }
        return conditionExpression;
    }

    public String extractSequenceFlowCondition(Element sequenceFlow) {
        var expression = sequenceFlow.getElementsByTagNameNS(BPMN_NAMESPACE, "conditionExpression");
        if (expression.getLength() == 0) {
            return "";
        }
        var expr = (Element) expression.item(0);
        return expr.getTextContent().trim();
    }
}
