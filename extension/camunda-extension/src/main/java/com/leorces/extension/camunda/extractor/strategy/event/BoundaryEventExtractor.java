package com.leorces.extension.camunda.extractor.strategy.event;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.boundary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BoundaryEventExtractor implements ActivityExtractionStrategy {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "boundaryEvent", parentId, processId, this::createBoundaryEvent);
    }

    private ActivityDefinition createBoundaryEvent(Element element, String parentId, String processId) {
        var timerDefinition = findTimerDefinition(element);
        if (timerDefinition != null) {
            return createTimerBoundaryEvent(element, parentId, timerDefinition);
        }

        var messageDefinition = helper.findMessageDefinition(element);
        if (messageDefinition != null) {
            return createMessageBoundaryEvent(element, parentId, messageDefinition);
        }

        var errorDefinition = helper.findErrorDefinition(element);
        if (errorDefinition != null) {
            return createErrorBoundaryEvent(element, parentId, errorDefinition);
        }

        var signalDefinition = findSignalDefinition(element);
        if (signalDefinition != null) {
            return createSignalBoundaryEvent(element, parentId, signalDefinition);
        }

        var conditionalDefinition = findConditionalDefinition(element);
        if (conditionalDefinition != null) {
            return createConditionalBoundaryEvent(element, parentId, conditionalDefinition);
        }

        var escalationDefinition = findEscalationDefinition(element);
        if (escalationDefinition != null) {
            return createEscalationBoundaryEvent(element, parentId, escalationDefinition);
        }

        throw new IllegalArgumentException("Unsupported boundary event type for element: " + element.getAttribute("id"));
    }

    private Element findTimerDefinition(Element element) {
        var timerDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "timerEventDefinition");
        return timerDefinitions.getLength() > 0 ? (Element) timerDefinitions.item(0) : null;
    }

    private Element findSignalDefinition(Element element) {
        var signalDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "signalEventDefinition");
        return signalDefinitions.getLength() > 0 ? (Element) signalDefinitions.item(0) : null;
    }

    private Element findConditionalDefinition(Element element) {
        var conditionalDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "conditionalEventDefinition");
        return conditionalDefinitions.getLength() > 0 ? (Element) conditionalDefinitions.item(0) : null;
    }

    private Element findEscalationDefinition(Element element) {
        var escalationDefinitions = element.getElementsByTagNameNS(BPMN_NAMESPACE, "escalationEventDefinition");
        return escalationDefinitions.getLength() > 0 ? (Element) escalationDefinitions.item(0) : null;
    }

    private TimerBoundaryEvent createTimerBoundaryEvent(Element element, String parentId, Element timerDefinition) {
        return TimerBoundaryEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .attachedToRef(element.getAttribute("attachedToRef"))
                .cancelActivity(!"false".equals(element.getAttribute("cancelActivity")))
                .timeDuration(getTimerAttribute(timerDefinition, "timeDuration"))
                .timeDate(getTimerAttribute(timerDefinition, "timeDate"))
                .timeCycle(getTimerAttribute(timerDefinition, "timeCycle"))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private MessageBoundaryEvent createMessageBoundaryEvent(Element element, String parentId, Element messageDefinition) {
        return MessageBoundaryEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .attachedToRef(element.getAttribute("attachedToRef"))
                .cancelActivity(!"false".equals(element.getAttribute("cancelActivity")))
                .messageReference(helper.getMessageName(messageDefinition))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private ErrorBoundaryEvent createErrorBoundaryEvent(Element element, String parentId, Element errorDefinition) {
        return ErrorBoundaryEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .attachedToRef(element.getAttribute("attachedToRef"))
                .cancelActivity(!"false".equals(element.getAttribute("cancelActivity")))
                .errorCode(helper.getErrorCode(errorDefinition))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private SignalBoundaryEvent createSignalBoundaryEvent(Element element, String parentId, Element signalDefinition) {
        return SignalBoundaryEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .attachedToRef(element.getAttribute("attachedToRef"))
                .cancelActivity(!"false".equals(element.getAttribute("cancelActivity")))
                .signalReference(signalDefinition.getAttribute("signalRef"))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private ConditionalBoundaryEvent createConditionalBoundaryEvent(Element element, String parentId, Element conditionalDefinition) {
        return ConditionalBoundaryEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .attachedToRef(element.getAttribute("attachedToRef"))
                .cancelActivity(!"false".equals(element.getAttribute("cancelActivity")))
                .condition(getConditionalExpression(conditionalDefinition))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private EscalationBoundaryEvent createEscalationBoundaryEvent(Element element, String parentId, Element escalationDefinition) {
        return EscalationBoundaryEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .attachedToRef(element.getAttribute("attachedToRef"))
                .cancelActivity(!"false".equals(element.getAttribute("cancelActivity")))
                .escalationCode(escalationDefinition.getAttribute("escalationRef"))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private String getTimerAttribute(Element timerDefinition, String attributeName) {
        var elements = timerDefinition.getElementsByTagNameNS(BPMN_NAMESPACE, attributeName);
        if (elements.getLength() > 0) {
            return elements.item(0).getTextContent();
        }
        return null;
    }

    private String getConditionalExpression(Element conditionalDefinition) {
        var conditions = conditionalDefinition.getElementsByTagNameNS(BPMN_NAMESPACE, "condition");
        if (conditions.getLength() > 0) {
            return conditions.item(0).getTextContent();
        }
        return null;
    }

}