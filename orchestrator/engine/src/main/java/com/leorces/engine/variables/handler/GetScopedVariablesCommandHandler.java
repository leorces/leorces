package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.persistence.VariablePersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetScopedVariablesCommandHandler implements ResultCommandHandler<GetScopedVariablesCommand, Map<String, Object>> {

    private final VariablePersistence variablePersistence;
    private final VariablesMapper variablesMapper;

    @Override
    public Map<String, Object> execute(GetScopedVariablesCommand command) {
        var activity = command.activity();
        var scope = activity.scope();
        var variables = variablePersistence.findInScope(activity.processId(), scope);
        return variablesMapper.toMap(variables, scope);
    }

    @Override
    public Class<GetScopedVariablesCommand> getCommandType() {
        return GetScopedVariablesCommand.class;
    }

}
