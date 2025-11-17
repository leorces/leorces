package com.leorces.extension.camunda.extractor.strategy.task;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.task.SendTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;

import static com.leorces.extension.camunda.BpmnConstants.*;

@Component
@RequiredArgsConstructor
public class SendTaskExtractor implements ActivityExtractionStrategy {

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(
                processElement,
                SEND_TASK,
                parentId,
                processId,
                this::createSendTask
        );
    }

    private SendTask createSendTask(Element element, String parentId, String processId) {
        return SendTask.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .topic(element.getAttributeNS(CAMUNDA_NAMESPACE, ATTRIBUTE_TOPIC))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

}