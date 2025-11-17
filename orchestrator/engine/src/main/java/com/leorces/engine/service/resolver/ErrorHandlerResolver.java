package com.leorces.engine.service.resolver;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ErrorActivityDefinition;
import com.leorces.model.definition.activity.event.boundary.ErrorBoundaryEvent;
import com.leorces.model.definition.activity.event.start.ErrorStartEvent;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlerResolver extends AbstractEventHandlerResolver<
        ErrorActivityDefinition,
        ErrorBoundaryEvent,
        ErrorStartEvent> {

    @Override
    protected ActivityType boundaryEventType() {
        return ActivityType.ERROR_BOUNDARY_EVENT;
    }

    @Override
    protected ActivityType startEventType() {
        return ActivityType.ERROR_START_EVENT;
    }

    @Override
    protected String codeOf(ErrorActivityDefinition definition) {
        return definition.errorCode();
    }

    @Override
    protected String parentIdOf(ErrorStartEvent startEvent) {
        return startEvent.parentId();
    }

    @Override
    protected String attachedToRefOf(ErrorBoundaryEvent boundaryEvent) {
        return boundaryEvent.attachedToRef();
    }

}
