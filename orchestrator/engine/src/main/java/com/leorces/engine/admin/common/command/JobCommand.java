package com.leorces.engine.admin.common.command;

import com.leorces.engine.core.ExecutionCommand;

import java.util.Map;

public interface JobCommand extends ExecutionCommand {
    Map<String, Object> input();

}
