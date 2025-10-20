package com.leorces.extension.camunda.extractor.strategy.task;


import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.task.ExternalTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ServiceTaskExtractor implements ActivityExtractionStrategy {

    private static final String CAMUNDA_NAMESPACE = "http://camunda.org/schema/1.0/bpmn";

    private final ActivityExtractionHelper helper;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "serviceTask", parentId, processId, this::createExternalTask);
    }

    private ExternalTask createExternalTask(Element element, String parentId, String processId) {
        var topic = element.getAttributeNS(CAMUNDA_NAMESPACE, "topic");
        return ExternalTask.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .topic(topic)
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

}