package com.leorces.extension.camunda.extractor.strategy;

import com.leorces.model.definition.activity.ActivityDefinition;
import org.w3c.dom.Element;

import java.util.List;

public interface ActivityExtractionStrategy {

    List<ActivityDefinition> extract(Element processElement, String parentId, String processId);

}
