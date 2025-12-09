package com.leorces.model.definition.activity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.definition.activity.event.boundary.*;
import com.leorces.model.definition.activity.event.end.*;
import com.leorces.model.definition.activity.event.intermediate.EscalationIntermediateThrowEvent;
import com.leorces.model.definition.activity.event.intermediate.IntermediateCatchEvent;
import com.leorces.model.definition.activity.event.intermediate.MessageIntermediateCatchEvent;
import com.leorces.model.definition.activity.event.start.ErrorStartEvent;
import com.leorces.model.definition.activity.event.start.EscalationStartEvent;
import com.leorces.model.definition.activity.event.start.MessageStartEvent;
import com.leorces.model.definition.activity.event.start.StartEvent;
import com.leorces.model.definition.activity.gateway.EventBasedGateway;
import com.leorces.model.definition.activity.gateway.ExclusiveGateway;
import com.leorces.model.definition.activity.gateway.InclusiveGateway;
import com.leorces.model.definition.activity.gateway.ParallelGateway;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.definition.activity.subprocess.EventSubprocess;
import com.leorces.model.definition.activity.subprocess.Subprocess;
import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.definition.activity.task.ReceiveTask;
import com.leorces.model.definition.activity.task.SendTask;

import java.io.IOException;

public class ActivityDefinitionDeserializer extends JsonDeserializer<ActivityDefinition> {

    @Override
    public ActivityDefinition deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        var mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        var typeNode = node.get("type");
        if (typeNode == null) {
            throw new IllegalArgumentException("Missing 'type' field in activity definition");
        }

        var type = ActivityType.valueOf(typeNode.asText());

        return switch (type) {
            case START_EVENT -> mapper.treeToValue(node, StartEvent.class);
            case MESSAGE_END_EVENT -> mapper.treeToValue(node, MessageEndEvent.class);
            case END_EVENT -> mapper.treeToValue(node, EndEvent.class);
            case MESSAGE_START_EVENT -> mapper.treeToValue(node, MessageStartEvent.class);
            case EXTERNAL_TASK -> mapper.treeToValue(node, ExternalTask.class);
            case SEND_TASK -> mapper.treeToValue(node, SendTask.class);
            case RECEIVE_TASK -> mapper.treeToValue(node, ReceiveTask.class);
            case PARALLEL_GATEWAY -> mapper.treeToValue(node, ParallelGateway.class);
            case EXCLUSIVE_GATEWAY -> mapper.treeToValue(node, ExclusiveGateway.class);
            case INCLUSIVE_GATEWAY -> mapper.treeToValue(node, InclusiveGateway.class);
            case ERROR_END_EVENT -> mapper.treeToValue(node, ErrorEndEvent.class);
            case ERROR_START_EVENT -> mapper.treeToValue(node, ErrorStartEvent.class);
            case TERMINATE_END_EVENT -> mapper.treeToValue(node, TerminateEndEvent.class);
            case SUBPROCESS -> mapper.treeToValue(node, Subprocess.class);
            case EVENT_SUBPROCESS -> mapper.treeToValue(node, EventSubprocess.class);
            case INTERMEDIATE_CATCH_EVENT -> mapper.treeToValue(node, IntermediateCatchEvent.class);
            case MESSAGE_INTERMEDIATE_CATCH_EVENT -> mapper.treeToValue(node, MessageIntermediateCatchEvent.class);
            case EVENT_BASED_GATEWAY -> mapper.treeToValue(node, EventBasedGateway.class);
            case CALL_ACTIVITY -> mapper.treeToValue(node, CallActivity.class);
            case ESCALATION_END_EVENT -> mapper.treeToValue(node, EscalationEndEvent.class);
            case ESCALATION_INTERMEDIATE_THROW_EVENT ->
                    mapper.treeToValue(node, EscalationIntermediateThrowEvent.class);
            case ESCALATION_START_EVENT -> mapper.treeToValue(node, EscalationStartEvent.class);
            case TIMER_BOUNDARY_EVENT -> mapper.treeToValue(node, TimerBoundaryEvent.class);
            case MESSAGE_BOUNDARY_EVENT -> mapper.treeToValue(node, MessageBoundaryEvent.class);
            case ERROR_BOUNDARY_EVENT -> mapper.treeToValue(node, ErrorBoundaryEvent.class);
            case SIGNAL_BOUNDARY_EVENT -> mapper.treeToValue(node, SignalBoundaryEvent.class);
            case CONDITIONAL_BOUNDARY_EVENT -> mapper.treeToValue(node, ConditionalBoundaryEvent.class);
            case ESCALATION_BOUNDARY_EVENT -> mapper.treeToValue(node, EscalationBoundaryEvent.class);
        };
    }

}

