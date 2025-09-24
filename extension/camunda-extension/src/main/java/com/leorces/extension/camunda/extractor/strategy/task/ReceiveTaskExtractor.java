package com.leorces.extension.camunda.extractor.strategy.task;


import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.task.ReceiveTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ReceiveTaskExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "receiveTask", parentId, processId, this::createReceiveTask);
    }

    private ReceiveTask createReceiveTask(Element element, String parentId, String processId) {
        var messageRef = element.getAttribute("messageRef");
        var messageName = helper.resolveMessageName(element, messageRef);

        return ReceiveTask.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .messageReference(messageName)
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }
}