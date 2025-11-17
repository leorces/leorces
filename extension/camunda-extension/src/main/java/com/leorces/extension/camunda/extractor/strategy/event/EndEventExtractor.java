package com.leorces.extension.camunda.extractor.strategy.event;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.end.EndEvent;
import com.leorces.model.definition.activity.event.end.ErrorEndEvent;
import com.leorces.model.definition.activity.event.end.EscalationEndEvent;
import com.leorces.model.definition.activity.event.end.TerminateEndEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
@RequiredArgsConstructor
public class EndEventExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(
                processElement,
                END_EVENT,
                parentId,
                processId,
                this::createEndEvent
        );
    }

    private ActivityDefinition createEndEvent(Element element, String parentId, String processId) {
        var errorDefinition = helper.findErrorDefinition(element);
        if (errorDefinition != null) {
            return createErrorEndEvent(element, parentId, errorDefinition);
        }

        var escalationDefinition = helper.findEscalationEventDefinition(element);
        if (escalationDefinition != null) {
            return createEscalationEndEvent(element, parentId, escalationDefinition);
        }

        if (hasTerminateDefinition(element)) {
            return createTerminateEndEvent(element, parentId);
        }

        return createBasicEndEvent(element, parentId);
    }

    private boolean hasTerminateDefinition(Element element) {
        return element.getElementsByTagNameNS(BPMN_NAMESPACE, TERMINATE_EVENT_DEFINITION).getLength() > 0;
    }

    private ErrorEndEvent createErrorEndEvent(Element element, String parentId, Element errorDefinition) {
        return ErrorEndEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .errorCode(helper.getErrorCode(errorDefinition))
                .build();
    }

    private TerminateEndEvent createTerminateEndEvent(Element element, String parentId) {
        return TerminateEndEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .build();
    }

    private EscalationEndEvent createEscalationEndEvent(Element element, String parentId, Element escalationDefinition) {
        return EscalationEndEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .escalationCode(helper.getEscalationCode(escalationDefinition))
                .build();
    }

    private EndEvent createBasicEndEvent(Element element, String parentId) {
        return EndEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .build();
    }

}