package com.leorces.engine.correlation.command;

import com.leorces.engine.core.ExecutionCommand;

import java.util.Map;

public record CorrelateMessageCommand(
        String messageName,
        String businessKey,
        Map<String, Object> correlationKeys,
        Map<String, Object> processVariables
) implements ExecutionCommand {

    public static CorrelateMessageCommand of(String messageName,
                                             String businessKey,
                                             Map<String, Object> correlationKeys,
                                             Map<String, Object> processVariables) {
        return new CorrelateMessageCommand(messageName, businessKey, correlationKeys, processVariables);
    }

    public static CorrelateMessageCommand of(String messageName,
                                             String businessKey) {
        return new CorrelateMessageCommand(messageName, businessKey, Map.of(), Map.of());
    }

}
