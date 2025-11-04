package com.leorces.extension.camunda;

import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionStrategy;
import com.leorces.model.definition.activity.ActivityDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BpmnActivityParser {

    private final List<ActivityExtractionStrategy> extractionStrategies;

    public List<ActivityDefinition> extractActivities(Element processElement, String parentId, String processId) {
        return extractionStrategies.stream()
                .flatMap(strategy -> strategy.extract(processElement, parentId, processId).stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

}