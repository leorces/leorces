package com.leorces.extension.camunda.extractor;

import com.leorces.extension.camunda.exception.BpmnParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Component responsible for parsing BPMN XML documents.
 */
@Slf4j
@Component
public class BpmnDocumentParser {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    /**
     * Parses an input stream into a DOM Document.
     *
     * @param inputStream the input stream to parse
     * @return parsed DOM document
     * @throws BpmnParseException if parsing fails
     */
    public Document parseDocument(InputStream inputStream) {
        try {
            var factory = createDocumentBuilderFactory();
            var builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException | IOException | org.xml.sax.SAXException | IllegalArgumentException e) {
            log.error("Failed to parse BPMN document", e);
            throw new BpmnParseException("Failed to parse BPMN document", e);
        }
    }

    /**
     * Finds the main process element in the document.
     *
     * @param document the DOM document
     * @return the process element
     * @throws BpmnParseException if no process element is found
     */
    public Element findProcessElement(Document document) {
        var processes = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");
        if (processes.getLength() == 0) {
            throw new BpmnParseException("No process element found in BPMN file");
        }
        return (Element) processes.item(0);
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }

}