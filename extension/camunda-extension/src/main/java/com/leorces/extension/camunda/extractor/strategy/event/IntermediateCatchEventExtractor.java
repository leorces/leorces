package com.leorces.extension.camunda.extractor.strategy.event;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.IntermediateCatchEvent;
import com.leorces.model.definition.activity.event.MessageIntermediateCatchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IntermediateCatchEventExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "intermediateCatchEvent", parentId, processId, this::createIntermediateCatchEvent);
    }

    private ActivityDefinition createIntermediateCatchEvent(Element element, String parentId, String processId) {
        var messageDefinition = helper.findMessageDefinition(element);
        if (messageDefinition != null) {
            return createMessageIntermediateCatchEvent(element, parentId, messageDefinition);
        }
        return createBasicIntermediateCatchEvent(element, parentId);
    }

    private MessageIntermediateCatchEvent createMessageIntermediateCatchEvent(Element element, String parentId, Element messageDefinition) {
        return MessageIntermediateCatchEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .messageReference(helper.getMessageName(messageDefinition))
                .build();
    }

    private IntermediateCatchEvent createBasicIntermediateCatchEvent(Element element, String parentId) {
        return IntermediateCatchEvent.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .condition(helper.extractCondition(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

}