package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.variables.command.GetProcessVariablesCommand;
import com.leorces.persistence.VariablePersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetProcessVariablesCommandHandler implements ResultCommandHandler<GetProcessVariablesCommand, Map<String, Object>> {

    private final VariablePersistence variablePersistence;
    private final VariablesMapper variablesMapper;

    @Override
    public Map<String, Object> execute(GetProcessVariablesCommand command) {
        var variables = variablePersistence.findInProcess(command.processId());
        return variablesMapper.toMap(variables);
    }

    @Override
    public Class<GetProcessVariablesCommand> getCommandType() {
        return GetProcessVariablesCommand.class;
    }

}
