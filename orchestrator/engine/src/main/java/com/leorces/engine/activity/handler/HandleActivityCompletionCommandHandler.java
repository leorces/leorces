package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithNextActivitiesCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleActivityCompletionCommandHandler implements CommandHandler<HandleActivityCompletionCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(HandleActivityCompletionCommand command) {
        var nextActivities = command.result().nextActivities();
        var activity = command.result().activity();

        if (nextActivities.isEmpty()) {
            dispatcher.dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(activity));
        } else {
            dispatcher.dispatch(HandleActivityCompletionWithNextActivitiesCommand.of(activity.process(), nextActivities));
        }
    }

    @Override
    public Class<HandleActivityCompletionCommand> getCommandType() {
        return HandleActivityCompletionCommand.class;
    }

}
