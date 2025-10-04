package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.MoveExecutionCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("MoveExecutionCommandHandler Tests")
class MoveExecutionCommandHandlerTest {

    private static final String PROCESS_ID = "process-1";
    private static final String ACTIVITY_ID = "activity-1";
    private static final String TARGET_DEFINITION_ID = "target-1";

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private MoveExecutionCommandHandler handler;

    private MoveExecutionCommand command;

    @BeforeEach
    void setUp() {
        command = MoveExecutionCommand.of(PROCESS_ID, ACTIVITY_ID, TARGET_DEFINITION_ID);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType()).isEqualTo(MoveExecutionCommand.class);
    }

    @Test
    @DisplayName("Should dispatch TerminateActivityCommand and RunActivityCommand in order")
    void shouldDispatchTerminateAndRunCommands() {
        // When
        handler.handle(command);

        // Then
        var terminateCaptor = ArgumentCaptor.forClass(TerminateActivityCommand.class);
        var runCaptor = ArgumentCaptor.forClass(RunActivityCommand.class);

        verify(dispatcher).dispatch(terminateCaptor.capture());
        verify(dispatcher).dispatch(runCaptor.capture());

        var terminateCommand = terminateCaptor.getValue();
        assertThat(terminateCommand.activityId()).isEqualTo(ACTIVITY_ID);
        assertThat(terminateCommand.withInterruption()).isTrue();

        var runCommand = runCaptor.getValue();
        assertThat(runCommand.processId()).isEqualTo(PROCESS_ID);
        assertThat(runCommand.definitionId()).isEqualTo(TARGET_DEFINITION_ID);

        verifyNoMoreInteractions(dispatcher);
    }

}
