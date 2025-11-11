package com.leorces.extension.camunda.extractor.strategy.event;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.intermediate.EscalationIntermediateThrowEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ThrowEventExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "intermediateThrowEvent", parentId, processId, this::createIntermediateThrowEvent);
    }

    private ActivityDefinition createIntermediateThrowEvent(Element element, String parentId, String processId) {
        var escalationDefinition = helper.findEscalationEventDefinition(element);
        if (escalationDefinition != null) {
            return createEscalationThrowEvent(element, parentId, escalationDefinition);
        }

        return null;
    }

    private ActivityDefinition createEscalationThrowEvent(Element element, String parentId, Element escalationDefinition) {
        return EscalationIntermediateThrowEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .escalationCode(helper.getEscalationCode(escalationDefinition))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

}
