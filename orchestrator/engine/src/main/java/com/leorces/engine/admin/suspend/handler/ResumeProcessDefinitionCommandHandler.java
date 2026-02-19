package com.leorces.engine.admin.suspend.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionByIdCommand;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionByKeyCommand;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeProcessDefinitionCommandHandler implements CommandHandler<ResumeProcessDefinitionCommand> {

    private static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    private static final String PROCESS_DEFINITION_VERSION = "processDefinitionVersion";

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(ResumeProcessDefinitionCommand command) {
        var input = command.input();

        if (!isInputValid(input)) {
            throw ExecutionException.of("Invalid input", "Cannot resume process definition");
        }

        var processDefinitionKey = input.get(PROCESS_DEFINITION_KEY).toString();
        var processDefinitionVersion = input.getOrDefault(PROCESS_DEFINITION_VERSION, null);

        if (processDefinitionVersion != null) {
            dispatcher.dispatchAsync(
                    new ResumeProcessDefinitionByIdCommand(
                            processDefinitionKey,
                            (int) processDefinitionVersion,
                            input
                    )
            );
        } else {
            dispatcher.dispatch(
                    new ResumeProcessDefinitionByKeyCommand(
                            processDefinitionKey,
                            input
                    )
            );
        }
    }

    @Override
    public Class<ResumeProcessDefinitionCommand> getCommandType() {
        return ResumeProcessDefinitionCommand.class;
    }

    private boolean isInputValid(Map<String, Object> input) {
        return input != null
                && !input.isEmpty()
                && input.get(PROCESS_DEFINITION_KEY) != null;
    }

}
