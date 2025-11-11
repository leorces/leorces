package com.leorces.engine.correlation.service;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.EscalationActivityDefinition;
import com.leorces.model.definition.activity.event.boundary.EscalationBoundaryEvent;
import com.leorces.model.definition.activity.event.start.EscalationStartEvent;
import org.springframework.stereotype.Component;

@Component
public class EscalationHandlerResolver extends AbstractEventHandlerResolver<
        EscalationActivityDefinition,
        EscalationBoundaryEvent,
        EscalationStartEvent> {

    @Override
    protected ActivityType boundaryEventType() {
        return ActivityType.ESCALATION_BOUNDARY_EVENT;
    }

    @Override
    protected ActivityType startEventType() {
        return ActivityType.ESCALATION_START_EVENT;
    }

    @Override
    protected String codeOf(EscalationActivityDefinition definition) {
        return definition.escalationCode();
    }

    @Override
    protected String parentIdOf(EscalationStartEvent startEvent) {
        return startEvent.parentId();
    }

    @Override
    protected String attachedToRefOf(EscalationBoundaryEvent boundaryEvent) {
        return boundaryEvent.attachedToRef();
    }

}
