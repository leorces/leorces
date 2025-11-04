package com.leorces.extension.camunda.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    public <T> List<T> extractElements(Element processElement,
                                       String elementName,
                                       String parentId,
                                       String processId,
                                       ElementCreator<T> creator) {
        var result = new ArrayList<T>();
        var nodes = processElement.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);

            if (!isTargetElement(node, elementName)) {
                continue;
            }

            var element = (Element) node;
            T created = creator.create(element, parentId, processId);
            result.add(created);
        }

        return result;
    }

    /**
     * Determines whether the given DOM node matches the desired BPMN element type and namespace.
     */
    private boolean isTargetElement(Node node, String elementName) {
        if (!(node instanceof Element element)) {
            return false;
        }
        return BPMN_NAMESPACE.equals(element.getNamespaceURI())
                && elementName.equals(element.getLocalName());
    }

    /**
     * Functional interface for creating objects from BPMN XML elements.
     */
    @FunctionalInterface
    public interface ElementCreator<T> {
        T create(Element element, String parentId, String processId);

    }

}
