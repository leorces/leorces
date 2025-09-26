package com.leorces.extension.camunda.extractor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * Component responsible for extracting BPMN elements from XML.
 */
@Slf4j
@Component
public class BpmnElementExtractor {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    /**
     * Extracts elements of specified type from process element.
     */
    public <T> List<T> extractElements(Element processElement, String elementName, String parentId, String processId, ElementCreator<T> creator) {
        var elements = processElement.getElementsByTagNameNS(BPMN_NAMESPACE, elementName);
        var result = new ArrayList<T>();
        for (int i = 0; i < elements.getLength(); i++) {
            var element = (Element) elements.item(i);
            if (isSubprocessChild(element) && parentId == null) {
                continue;
            }
            result.add(creator.create(element, parentId, processId));
        }
        return result;
    }

    private boolean isSubprocessChild(Element element) {
        var parent = element.getParentNode();
        return parent instanceof Element && "subProcess".equals(parent.getLocalName());
    }

    /**
     * Functional interface for creating activity definitions from XML elements.
     */
    @FunctionalInterface
    public interface ElementCreator<T> {

        T create(Element element, String parentId, String processId);

    }

}