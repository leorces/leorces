package com.leorces.extension.camunda.extractor.strategy.gateway;


import com.leorces.extension.camunda.extractor.BpmnConditionExtractor;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.gateway.InclusiveGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class InclusiveGatewayExtractor implements ActivityExtractionStrategy {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final ActivityExtractionHelper helper;
    private final BpmnConditionExtractor conditionExtractor;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "inclusiveGateway", parentId, processId, this::createInclusiveGateway);
    }

    private InclusiveGateway createInclusiveGateway(Element element, String parentId, String processId) {
        return InclusiveGateway.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .condition(buildInclusiveConditions(element))
                .build();
    }

    private Map<String, List<String>> buildInclusiveConditions(Element gateway) {
        var conditions = new HashMap<String, List<String>>();
        for (var sequenceFlow : findOutgoingSequenceFlows(gateway)) {
            var condition = conditionExtractor.extractSequenceFlowCondition(sequenceFlow);
            var target = sequenceFlow.getAttribute("targetRef");
            conditions.computeIfAbsent(condition, k -> new ArrayList<>()).add(target);
        }
        return conditions;
    }

    private List<Element> findOutgoingSequenceFlows(Element gateway) {
        var result = new ArrayList<Element>();
        var document = gateway.getOwnerDocument();
        var sequenceFlows = document.getElementsByTagNameNS(BPMN_NAMESPACE, "sequenceFlow");
        var sourceId = gateway.getAttribute("id");

        for (int i = 0; i < sequenceFlows.getLength(); i++) {
            var sequenceFlow = (Element) sequenceFlows.item(i);
            if (sourceId.equals(sequenceFlow.getAttribute("sourceRef"))) {
                result.add(sequenceFlow);
            }
        }
        return result;
    }
}