package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.admin.migration.command.SingleProcessMigrationCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.process.ProcessExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SingleProcessMigrationCommandHandler implements CommandHandler<SingleProcessMigrationCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(SingleProcessMigrationCommand command) {
        var process = command.process();
        var migration = command.migration();
        var instructions = migration.instructions();

        if (instructions.isEmpty()) {
            return;
        }

        for (var instruction : instructions) {
            handleInstruction(process, instruction);
        }
    }

    @Override
    public Class<SingleProcessMigrationCommand> getCommandType() {
        return SingleProcessMigrationCommand.class;
    }

    private void handleInstruction(ProcessExecution process, ActivityMigrationInstruction instruction) {
        var activeFromActivities = getNotCompletedActivities(process, instruction.fromActivityId());
        if (activeFromActivities.isEmpty()) {
            return;
        }

        for (var activity : activeFromActivities) {
            dispatcher.dispatch(DeleteActivityCommand.of(activity.id()));
            dispatcher.dispatch(RunActivityCommand.of(instruction.toActivityId(), process.id()));
        }
    }

    private List<Activity> getNotCompletedActivities(ProcessExecution process, String activityDefinitionId) {
        return process.getActivitiesByDefinitionId(activityDefinitionId).stream()
                .filter(activity -> !activity.isInTerminalState())
                .toList();
    }

}
