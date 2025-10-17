package com.leorces.extension.camunda.extractor.strategy.task;


import com.leorces.extension.camunda.configuration.properties.CamundaProperties;
import com.leorces.extension.camunda.configuration.properties.ProcessProperties;
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
    private final CamundaProperties properties;

    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return helper.extractElements(processElement, "serviceTask", parentId, processId, this::createExternalTask);
    }

    private ExternalTask createExternalTask(Element element, String parentId, String processId) {
        var topic = element.getAttributeNS(CAMUNDA_NAMESPACE, "topic");
        var retries = resolveRetries(topic, processId);
        var timeout = resolveTimeout(topic, processId);

        return ExternalTask.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .topic(topic)
                .retries(retries)
                .timeout(timeout)
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }

    private int resolveRetries(String topic, String processId) {
        if (processId == null || properties == null || properties.processes() == null) {
            return 0;
        }

        var processProperties = properties.processes().get(processId);
        if (processProperties == null) {
            return 0;
        }

        return resolveTaskRetries(processProperties, topic);
    }

    private String resolveTimeout(String topic, String processId) {
        if (processId == null || properties == null || properties.processes() == null) {
            return null;
        }

        var processProperties = properties.processes().get(processId);
        if (processProperties == null) {
            return null;
        }

        return resolveTaskTimeout(processProperties, topic);
    }

    private int resolveTaskRetries(ProcessProperties processProperties, String topic) {
        if (processProperties.tasks() != null && processProperties.tasks().containsKey(topic)) {
            return processProperties.tasks().get(topic).retries();
        }
        return processProperties.taskRetries();
    }

    private String resolveTaskTimeout(ProcessProperties processProperties, String topic) {
        if (processProperties.tasks() != null && processProperties.tasks().containsKey(topic)) {
            return processProperties.tasks().get(topic).timeout();
        }

        return processProperties.taskTimeout();
    }

}