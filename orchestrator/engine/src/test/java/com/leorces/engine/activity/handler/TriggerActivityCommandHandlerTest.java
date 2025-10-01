package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TriggerActivityCommandHandler Tests")
class TriggerActivityCommandHandlerTest {

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private TriggerableActivityBehaviour activityBehavior;

    @Mock
    private ActivityDefinition definition;

    @Mock
    private Process process;

    @InjectMocks
    private TriggerActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(definition.id()).thenReturn("activity-1");
        when(definition.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(process.id()).thenReturn("process-1");
    }

    @Test
    @DisplayName("Handle should trigger behavior if process is not in terminal state")
    void handleShouldTriggerBehaviorIfProcessActive() {
        // Given
        when(process.isInTerminalState()).thenReturn(false);
        when(behaviorResolver.resolveTriggerableBehavior(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(activityBehavior));

        var command = new TriggerActivityCommand(process, definition);

        // When
        handler.handle(command);

        // Then
        verify(activityBehavior).trigger(process, definition);
    }

    @Test
    @DisplayName("Handle should not trigger behavior if process is in terminal state")
    void handleShouldNotTriggerBehaviorIfProcessTerminated() {
        // Given
        when(process.isInTerminalState()).thenReturn(true);
        var command = new TriggerActivityCommand(process, definition);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver, never()).resolveTriggerableBehavior(any());
        verifyNoInteractions(activityBehavior);
    }

    @Test
    @DisplayName("Handle should not trigger if behavior is not present")
    void handleShouldNotTriggerIfBehaviorNotResolved() {
        // Given
        when(process.isInTerminalState()).thenReturn(false);
        when(behaviorResolver.resolveTriggerableBehavior(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.empty());

        var command = new TriggerActivityCommand(process, definition);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(activityBehavior);
    }

}
