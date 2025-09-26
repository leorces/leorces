package com.leorces.extension.camunda.extractor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Component responsible for extracting flow references from BPMN elements.
 */
@Slf4j
@Component
public class BpmnFlowExtractor {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    /**
     * Extracts incoming neighbor element IDs from a BPMN element.
     *
     * @param element the BPMN element
     * @return list of incoming neighbor element IDs
     */
    public List<String> extractIncoming(Element element) {
        var flowIds = extractFlowIds(element, "incoming");
        return mapToNeighborElementIds(element, flowIds, true);
    }

    /**
     * Extracts outgoing neighbor element IDs from a BPMN element.
     *
     * @param element the BPMN element
     * @return list of outgoing neighbor element IDs
     */
    public List<String> extractOutgoing(Element element) {
        var flowIds = extractFlowIds(element, "outgoing");
        return mapToNeighborElementIds(element, flowIds, false);
    }

    private List<String> extractFlowIds(Element element, String tagName) {
        var ids = new ArrayList<String>();
        var flowElements = element.getElementsByTagNameNS("*", tagName);
        for (int i = 0; i < flowElements.getLength(); i++) {
            var flowElement = (Element) flowElements.item(i);
            if (flowElement.getParentNode().equals(element)) {
                var id = flowElement.getTextContent().trim();
                if (!id.isEmpty()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private List<String> mapToNeighborElementIds(Element element, List<String> flowIds, boolean incomingDirection) {
        var result = new ArrayList<String>();
        for (var flowId : flowIds) {
            findSequenceFlow(element, flowId)
                    .map(seq -> neighborId(seq, incomingDirection))
                    .ifPresent(result::add);
        }
        return result;
    }

    private Optional<Element> findSequenceFlow(Element element, String flowId) {
        var doc = element.getOwnerDocument();
        var seqFlows = doc.getElementsByTagNameNS(BPMN_NAMESPACE, "sequenceFlow");
        for (int i = 0; i < seqFlows.getLength(); i++) {
            var seq = (Element) seqFlows.item(i);
            if (flowId.equals(seq.getAttribute("id"))) {
                return Optional.of(seq);
            }
        }
        return Optional.empty();
    }

    private String neighborId(Element sequenceFlow, boolean incomingDirection) {
        return incomingDirection
                ? sequenceFlow.getAttribute("sourceRef")
                : sequenceFlow.getAttribute("targetRef");
    }

}