package com.leorces.extension.camunda.extractor.strategy.subprocess;


import com.leorces.extension.camunda.BpmnActivityParser;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.subprocess.EventSubprocess;
import com.leorces.model.definition.activity.subprocess.Subprocess;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;


@Component
public class SubprocessExtractor implements ActivityExtractionStrategy {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final ActivityExtractionHelper helper;
    private final BpmnActivityParser activityParser;

    public SubprocessExtractor(ActivityExtractionHelper helper,
                               @Lazy BpmnActivityParser activityParser) {
        this.helper = helper;
        this.activityParser = activityParser;
    }


    @Override
    public List<ActivityDefinition> extract(Element processElement, String parentId, String processId) {
        return extractSubprocesses(processElement, parentId, processId);
    }

    public List<ActivityDefinition> extractSubprocesses(Element processElement, String parentId, String processId) {
        var subprocesses = processElement.getElementsByTagNameNS(BPMN_NAMESPACE, "subProcess");
        var result = new ArrayList<ActivityDefinition>();

        for (int i = 0; i < subprocesses.getLength(); i++) {
            var element = (Element) subprocesses.item(i);
            result.add(createSubprocess(element, parentId));
            result.addAll(activityParser.extractActivities(element, element.getAttribute("id"), processId));
        }

        return result;
    }

    private ActivityDefinition createSubprocess(Element element, String parentId) {
        return isEventSubprocess(element)
                ? createEventSubprocess(element, parentId)
                : createBasicSubprocess(element, parentId);
    }

    private boolean isEventSubprocess(Element element) {
        var triggered = element.getAttribute("triggeredByEvent");
        return "true".equalsIgnoreCase(triggered);
    }

    private EventSubprocess createEventSubprocess(Element element, String parentId) {
        return EventSubprocess.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .build();
    }

    private Subprocess createBasicSubprocess(Element element, String parentId) {
        return Subprocess.builder()
                .id(helper.getId(element))
                .parentId(parentId)
                .name(helper.getName(element))
                .incoming(helper.extractIncoming(element))
                .outgoing(helper.extractOutgoing(element))
                .inputs(helper.extractInputParameters(element))
                .outputs(helper.extractOutputParameters(element))
                .build();
    }
}