package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.variables.command.EvaluateVariablesCommand;
import com.leorces.engine.variables.command.SetActivityVariablesCommand;
import com.leorces.engine.variables.command.SetVariablesCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetActivityVariablesCommandHandler implements CommandHandler<SetActivityVariablesCommand> {

    private final VariablesMapper variablesMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(SetActivityVariablesCommand command) {
        var activity = command.activity();
        var variables = command.variables();

        var outputVariables = dispatcher.execute(EvaluateVariablesCommand.of(activity, activity.outputs()));
        var outputVariablesMap = variablesMapper.toMap(outputVariables);
        var combinedVariables = combineVariables(variables, outputVariablesMap);
        dispatcher.dispatch(SetVariablesCommand.of(activity.process(), combinedVariables));
    }

    @Override
    public Class<SetActivityVariablesCommand> getCommandType() {
        return SetActivityVariablesCommand.class;
    }

    private Map<String, Object> combineVariables(Map<String, Object> inputVariables, Map<String, Object> outputVariables) {
        var combined = new HashMap<>(inputVariables);
        combined.putAll(outputVariables);
        return combined;
    }

}
