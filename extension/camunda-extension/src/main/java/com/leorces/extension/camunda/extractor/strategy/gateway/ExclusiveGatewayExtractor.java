package com.leorces.extension.camunda.extractor.strategy.gateway;

import com.leorces.extension.camunda.extractor.BpmnConditionExtractor;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.gateway.ExclusiveGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.leorces.extension.camunda.BpmnConstants.ATTRIBUTE_TARGET_REF;
import static com.leorces.extension.camunda.BpmnConstants.EXCLUSIVE_GATEWAY;

@Component
@RequiredArgsConstructor
public class ExclusiveGatewayExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;
    private final BpmnConditionExtractor conditionExtractor;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(
                processElement,
                EXCLUSIVE_GATEWAY,
                parentId,
                processId,
                this::createExclusiveGateway
        );
    }

    private ExclusiveGateway createExclusiveGateway(Element element, String parentId, String processId) {
        return ExclusiveGateway.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .condition(buildConditions(element))
                .build();
    }

    private Map<String, String> buildConditions(Element gateway) {
        var conditions = new HashMap<String, String>();
        for (var sequenceFlow : helper.extractGatewayOutgoing(gateway)) {
            var condition = conditionExtractor.extractSequenceFlowCondition(sequenceFlow);
            var target = sequenceFlow.getAttribute(ATTRIBUTE_TARGET_REF);
            conditions.put(condition, target);
        }
        return conditions;
    }

}