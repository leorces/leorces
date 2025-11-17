package com.leorces.extension.camunda.extractor.strategy.event;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.start.ErrorStartEvent;
import com.leorces.model.definition.activity.event.start.EscalationStartEvent;
import com.leorces.model.definition.activity.event.start.MessageStartEvent;
import com.leorces.model.definition.activity.event.start.StartEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
@RequiredArgsConstructor
public class StartEventExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(
                processElement,
                START_EVENT,
                parentId,
                processId,
                this::createStartEvent
        );
    }

    private ActivityDefinition createStartEvent(Element element, String parentId, String processId) {
        var messageDefinition = helper.findMessageDefinition(element);
        if (messageDefinition != null) {
            return createMessageStartEvent(element, parentId, messageDefinition);
        }

        var errorDefinition = helper.findErrorDefinition(element);
        if (errorDefinition != null) {
            return createErrorStartEvent(element, parentId, errorDefinition);
        }

        var escalationDefinition = helper.findEscalationEventDefinition(element);
        if (escalationDefinition != null) {
            return createEscalationStartEvent(element, parentId, escalationDefinition);
        }

        return createBasicStartEvent(element, parentId);
    }

    private MessageStartEvent createMessageStartEvent(Element element, String parentId, Element messageDefinition) {
        return MessageStartEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .messageReference(helper.getMessageName(messageDefinition))
                .isInterrupting(!FALSE_VALUE.equals(element.getAttribute(ATTRIBUTE_IS_INTERRUPTING)))
                .build();
    }

    private ErrorStartEvent createErrorStartEvent(Element element, String parentId, Element errorDefinition) {
        return ErrorStartEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .errorCode(helper.getErrorCode(errorDefinition))
                .build();
    }

    private EscalationStartEvent createEscalationStartEvent(Element element, String parentId, Element escalationDefinition) {
        return EscalationStartEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .escalationCode(helper.getEscalationCode(escalationDefinition))
                .isInterrupting(!FALSE_VALUE.equals(element.getAttribute(ATTRIBUTE_IS_INTERRUPTING)))
                .build();
    }

    private StartEvent createBasicStartEvent(Element element, String parentId) {
        return StartEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .build();
    }

}
