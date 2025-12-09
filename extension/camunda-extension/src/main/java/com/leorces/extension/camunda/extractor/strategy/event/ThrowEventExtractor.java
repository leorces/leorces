package com.leorces.extension.camunda.extractor.strategy.event;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.intermediate.EscalationIntermediateThrowEvent;
import com.leorces.model.definition.activity.event.intermediate.MessageIntermediateThrowEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

import static com.leorces.extension.camunda.BpmnConstants.ATTRIBUTE_ID;
import static com.leorces.extension.camunda.BpmnConstants.INTERMEDIATE_THROW_EVENT;

@Component
@RequiredArgsConstructor
public class ThrowEventExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(
                processElement,
                INTERMEDIATE_THROW_EVENT,
                parentId,
                processId,
                this::createIntermediateThrowEvent
        );
    }

    private ActivityDefinition createIntermediateThrowEvent(Element element, String parentId, String processId) {
        var escalationDefinition = helper.findEscalationEventDefinition(element);
        if (escalationDefinition != null) {
            return createEscalationThrowEvent(element, parentId, escalationDefinition);
        }

        var messageDefinition = helper.findMessageDefinition(element);
        if (messageDefinition != null) {
            return createMessageThrowEvent(element, parentId, messageDefinition);
        }

        throw new IllegalArgumentException("Unsupported throw event type for element: %s".formatted(element.getAttribute(ATTRIBUTE_ID)));
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

    private MessageIntermediateThrowEvent createMessageThrowEvent(Element element, String parentId, Element messageDefinition) {
        return MessageIntermediateThrowEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .topic(helper.getTopic(messageDefinition))
                .messageReference(helper.getMessageName(messageDefinition))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

}
