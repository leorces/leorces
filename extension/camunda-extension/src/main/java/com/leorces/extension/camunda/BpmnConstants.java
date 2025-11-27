package com.leorces.extension.camunda;

/**
 * Constants for BPMN XML processing.
 */
public final class BpmnConstants {
    // =====================
    // Namespaces
    // =====================
    public static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    public static final String CAMUNDA_NAMESPACE = "http://camunda.org/schema/1.0/bpmn";
    // =====================
    // BPMN Elements
    // =====================
    public static final String PROCESS = "process";
    public static final String SEQUENCE_FLOW = "sequenceFlow";
    public static final String INCOMING = "incoming";
    public static final String OUTGOING = "outgoing";
    public static final String SERVICE_TASK = "serviceTask";
    public static final String SEND_TASK = "sendTask";
    public static final String RECEIVE_TASK = "receiveTask";
    public static final String SUB_PROCESS = "subProcess";
    public static final String CALL_ACTIVITY = "callActivity";
    public static final String INTERMEDIATE_THROW_EVENT = "intermediateThrowEvent";
    public static final String BOUNDARY_EVENT = "boundaryEvent";
    public static final String END_EVENT = "endEvent";
    public static final String START_EVENT = "startEvent";
    public static final String INTERMEDIATE_CATCH_EVENT = "intermediateCatchEvent";
    public static final String TERMINATE_EVENT_DEFINITION = "terminateEventDefinition";

    // Gateways
    public static final String EVENT_BASED_GATEWAY = "eventBasedGateway";
    public static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
    public static final String INCLUSIVE_GATEWAY = "inclusiveGateway";
    public static final String PARALLEL_GATEWAY = "parallelGateway";
    // =====================
    // Event Definitions
    // =====================
    public static final String TIMER_EVENT_DEFINITION = "timerEventDefinition";
    public static final String CONDITIONAL_EVENT_DEFINITION = "conditionalEventDefinition";
    public static final String MESSAGE_EVENT_DEFINITION = "messageEventDefinition";
    public static final String ERROR_EVENT_DEFINITION = "errorEventDefinition";
    public static final String ESCALATION_EVENT_DEFINITION = "escalationEventDefinition";
    public static final String SIGNAL_EVENT_DEFINITION = "signalEventDefinition";

    // Event-specific elements
    public static final String CONDITION = "condition";
    public static final String CONDITION_EXPRESSION = "conditionExpression";
    public static final String MESSAGE = "message";
    public static final String BPMN_MESSAGE = "bpmn:message";
    public static final String ERROR = "error";
    public static final String ESCALATION = "escalation";
    public static final String SIGNAL = "signal";
    // =====================
    // Extension Elements
    // =====================
    public static final String EXTENSION_ELEMENTS = "extensionElements";
    public static final String INPUT_OUTPUT = "inputOutput";
    public static final String INPUT_PARAMETER = "inputParameter";
    public static final String OUTPUT_PARAMETER = "outputParameter";
    // =====================
    // Attributes
    // =====================
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_ERROR_REF = "errorRef";
    public static final String ATTRIBUTE_ERROR_CODE = "errorCode";
    public static final String ATTRIBUTE_ESCALATION_REF = "escalationRef";
    public static final String ATTRIBUTE_ESCALATION_CODE = "escalationCode";
    public static final String ATTRIBUTE_SOURCE_REF = "sourceRef";
    public static final String ATTRIBUTE_TARGET_REF = "targetRef";
    public static final String ATTRIBUTE_TOPIC = "topic";
    public static final String ATTRIBUTE_MESSAGE_REF = "messageRef";
    public static final String ATTRIBUTE_TRIGGERED_BY_EVENT = "triggeredByEvent";
    public static final String ATTRIBUTE_CALLED_ELEMENT = "calledElement";
    public static final String ATTRIBUTE_CALLED_ELEMENT_VERSION = "calledElementVersion";
    public static final String ATTRIBUTE_SOURCE = "source";
    public static final String ATTRIBUTE_TARGET = "target";
    public static final String ATTRIBUTE_SOURCE_EXPRESSION = "sourceExpression";
    public static final String ATTRIBUTE_VARIABLES = "variables";
    public static final String ATTRIBUTE_ATTACHED_TO_REF = "attachedToRef";
    public static final String ATTRIBUTE_CANCEL_ACTIVITY = "cancelActivity";
    public static final String ATTRIBUTE_IS_INTERRUPTING = "isInterrupting";
    // =====================
    // Mappings
    // =====================
    public static final String MAPPING_IN = "in";
    public static final String MAPPING_OUT = "out";
    // =====================
    // Misc
    // =====================
    public static final String TRUE_VALUE = "true";
    public static final String FALSE_VALUE = "false";
    public static final String EMPTY_STRING = "";
    public static final String VALUE = "value";
    public static final String LIST = "list";

    private BpmnConstants() {
        // prevent instantiation
    }

}
