package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.HandleActivityCompletionWithNextActivitiesCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HandleActivityCompletionWithNextActivitiesCommandHandler Tests")
class HandleActivityCompletionWithNextActivitiesCommandHandlerTest {

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private Process process;

    @Mock
    private ActivityDefinition activity1;

    @Mock
    private ActivityDefinition activity2;

    @InjectMocks
    private HandleActivityCompletionWithNextActivitiesCommandHandler handler;

    @BeforeEach
    void setup() {
        when(process.id()).thenReturn("process-id");
        when(process.state()).thenReturn(ProcessState.ACTIVE);
    }

    @Test
    @DisplayName("Should run next activities when process is active")
    void shouldRunNextActivities() {
        var command = HandleActivityCompletionWithNextActivitiesCommand.of(process, List.of(activity1, activity2));

        handler.handle(command);

        verify(dispatcher, never()).dispatch(any(ResolveProcessIncidentCommand.class));
        verify(dispatcher, times(2)).dispatchAsync(any(RunActivityCommand.class));
    }

    @Test
    @DisplayName("Should resolve process incident and run activities when process in INCIDENT state")
    void shouldResolveIncidentAndRunActivities() {
        when(process.state()).thenReturn(ProcessState.INCIDENT);
        var command = HandleActivityCompletionWithNextActivitiesCommand.of(process, List.of(activity1));

        handler.handle(command);

        verify(dispatcher).dispatch(ResolveProcessIncidentCommand.of("process-id"));
        verify(dispatcher).dispatchAsync(any(RunActivityCommand.class));
    }

}
