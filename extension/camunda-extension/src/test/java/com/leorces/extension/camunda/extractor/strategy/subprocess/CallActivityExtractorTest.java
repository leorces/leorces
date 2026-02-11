package com.leorces.extension.camunda.extractor.strategy.subprocess;

import com.leorces.extension.camunda.extractor.BpmnElementExtractor;
import com.leorces.extension.camunda.extractor.strategy.ActivityExtractionHelper;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

import static com.leorces.extension.camunda.BpmnConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallActivityExtractorTest {

    @Mock
    private ActivityExtractionHelper helper;

    @InjectMocks
    private CallActivityExtractor extractor;

    @Test
    @DisplayName("should not add VariableMapping if all fields are null")
    @SuppressWarnings("unchecked")
    void shouldNotAddVariableMappingIfAllFieldsAreNull() {
        // Given
        var processElement = mock(Element.class);
        var callActivityElement = mock(Element.class);
        var extensionElementsList = mock(NodeList.class);
        var extensionElement = mock(Element.class);
        var mappingInList = mock(NodeList.class);
        var mappingInElement = mock(Element.class);

        when(helper.extractElements(eq(processElement), eq(CALL_ACTIVITY), any(), any(), any()))
                .thenAnswer(invocation -> {
                    var creator = (BpmnElementExtractor.ElementCreator<CallActivity>) invocation.getArgument(4);
                    return List.of(creator.create(callActivityElement, "parent", "process"));
                });

        when(callActivityElement.getElementsByTagNameNS(BPMN_NAMESPACE, EXTENSION_ELEMENTS)).thenReturn(extensionElementsList);
        when(extensionElementsList.getLength()).thenReturn(1);
        when(extensionElementsList.item(0)).thenReturn(extensionElement);

        when(extensionElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, MAPPING_IN)).thenReturn(mappingInList);
        when(mappingInList.getLength()).thenReturn(1);
        when(mappingInList.item(0)).thenReturn(mappingInElement);

        when(mappingInElement.getAttribute(ATTRIBUTE_SOURCE)).thenReturn("");
        when(mappingInElement.getAttribute(ATTRIBUTE_TARGET)).thenReturn("");
        when(mappingInElement.getAttribute(ATTRIBUTE_SOURCE_EXPRESSION)).thenReturn("");
        when(mappingInElement.getAttribute(ATTRIBUTE_VARIABLES)).thenReturn("");

        when(extensionElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, MAPPING_OUT)).thenReturn(mock(NodeList.class));
        when(callActivityElement.getElementsByTagNameNS(BPMN_NAMESPACE, ATTRIBUTE_MULTI_INSTANCE_LOOP_CHARACTERISTICS)).thenReturn(mock(NodeList.class));
        when(callActivityElement.getAttribute(ATTRIBUTE_CALLED_ELEMENT)).thenReturn("called");
        when(callActivityElement.getAttributeNS(CAMUNDA_NAMESPACE, ATTRIBUTE_CALLED_ELEMENT_VERSION)).thenReturn("");

        // When
        var result = extractor.extract(processElement, "parent", "process");

        // Then
        assertThat(result).hasSize(1);
        var callActivity = (CallActivity) result.getFirst();
        assertThat(callActivity.inputMappings()).isEmpty();
    }

    @Test
    @DisplayName("should add VariableMapping if at least one field is not null")
    @SuppressWarnings("unchecked")
    void shouldAddVariableMappingIfAtLeastOneFieldIsNotNull() {
        // Given
        var processElement = mock(Element.class);
        var callActivityElement = mock(Element.class);
        var extensionElementsList = mock(NodeList.class);
        var extensionElement = mock(Element.class);
        var mappingInList = mock(NodeList.class);
        var mappingInElement = mock(Element.class);

        when(helper.extractElements(eq(processElement), eq(CALL_ACTIVITY), any(), any(), any()))
                .thenAnswer(invocation -> {
                    var creator = (BpmnElementExtractor.ElementCreator<CallActivity>) invocation.getArgument(4);
                    return List.of(creator.create(callActivityElement, "parent", "process"));
                });

        when(callActivityElement.getElementsByTagNameNS(BPMN_NAMESPACE, EXTENSION_ELEMENTS)).thenReturn(extensionElementsList);
        when(extensionElementsList.getLength()).thenReturn(1);
        when(extensionElementsList.item(0)).thenReturn(extensionElement);

        when(extensionElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, MAPPING_IN)).thenReturn(mappingInList);
        when(mappingInList.getLength()).thenReturn(1);
        when(mappingInList.item(0)).thenReturn(mappingInElement);

        when(mappingInElement.getAttribute(ATTRIBUTE_SOURCE)).thenReturn("source");
        when(mappingInElement.getAttribute(ATTRIBUTE_TARGET)).thenReturn("");
        when(mappingInElement.getAttribute(ATTRIBUTE_SOURCE_EXPRESSION)).thenReturn("");
        when(mappingInElement.getAttribute(ATTRIBUTE_VARIABLES)).thenReturn("");

        when(extensionElement.getElementsByTagNameNS(CAMUNDA_NAMESPACE, MAPPING_OUT)).thenReturn(mock(NodeList.class));
        when(callActivityElement.getElementsByTagNameNS(BPMN_NAMESPACE, ATTRIBUTE_MULTI_INSTANCE_LOOP_CHARACTERISTICS)).thenReturn(mock(NodeList.class));
        when(callActivityElement.getAttribute(ATTRIBUTE_CALLED_ELEMENT)).thenReturn("called");
        when(callActivityElement.getAttributeNS(CAMUNDA_NAMESPACE, ATTRIBUTE_CALLED_ELEMENT_VERSION)).thenReturn("");

        // When
        var result = extractor.extract(processElement, "parent", "process");

        // Then
        assertThat(result).hasSize(1);
        var callActivity = (CallActivity) result.getFirst();
        assertThat(callActivity.inputMappings()).hasSize(1);
        assertThat(callActivity.inputMappings().getFirst().source()).isEqualTo("source");
    }
}
