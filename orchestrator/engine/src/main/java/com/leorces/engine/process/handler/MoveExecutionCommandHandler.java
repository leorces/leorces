package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.MoveExecutionCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoveExecutionCommandHandler implements CommandHandler<MoveExecutionCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(MoveExecutionCommand command) {
        log.debug("Move execution from activity: {} to: {} for process: {}", command.activityId(), command.targetDefinitionId(), command.processId());
        dispatcher.dispatch(TerminateActivityCommand.of(command.activityId(), true));
        dispatcher.dispatch(RunActivityCommand.of(command.targetDefinitionId(), command.processId()));
        log.debug("Move execution from activity: {} to: {} for process: {} success", command.activityId(), command.targetDefinitionId(), command.processId());
    }

    @Override
    public Class<MoveExecutionCommand> getCommandType() {
        return MoveExecutionCommand.class;
    }

}
