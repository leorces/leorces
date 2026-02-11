package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.admin.migration.command.SingleProcessMigrationCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SingleProcessMigrationCommandHandler implements CommandHandler<SingleProcessMigrationCommand> {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(SingleProcessMigrationCommand command) {
        var process = command.process();
        var toDefinition = command.toDefinition();
        var instructions = command.migration().instructions();

        if (instructions.isEmpty()) {
            updateProcessDefinition(process, toDefinition.id());
            return;
        }

        var fromActivities = instructions.stream().map(ActivityMigrationInstruction::fromActivityId).toList();
        var activitiesToMigrate = getNotCompletedActivities(process, fromActivities);

        deleteActivities(process, fromActivities, activitiesToMigrate);
        updateProcessDefinition(process, toDefinition.id());
        migrateActivities(process, activitiesToMigrate, instructions);
    }

    private void deleteActivities(ProcessExecution process,
                                  List<String> fromActivities,
                                  List<Activity> activitiesToMigrate) {
        var activitiesToDelete = getActivitiesToDelete(process, fromActivities, activitiesToMigrate);
        var activityIdsToDelete = Stream.concat(activitiesToDelete.stream(), activitiesToMigrate.stream())
                .map(Activity::id)
                .toList();
        activityPersistence.findAll(activityIdsToDelete)
                .forEach(activity -> dispatcher.dispatch(DeleteActivityCommand.of(activity)));
    }

    private void updateProcessDefinition(ProcessExecution process, String toDefinitionId) {
        processPersistence.updateDefinitionId(toDefinitionId, List.of(process.id()));
    }

    @Override
    public Class<SingleProcessMigrationCommand> getCommandType() {
        return SingleProcessMigrationCommand.class;
    }

    private void migrateActivities(ProcessExecution process,
                                   List<Activity> activitiesToMigrate,
                                   List<ActivityMigrationInstruction> instructions) {
        var activitiesToMigrateDefinitionIds = activitiesToMigrate.stream()
                .map(Activity::definitionId)
                .collect(Collectors.toSet());

        instructions.stream()
                .filter(instruction -> activitiesToMigrateDefinitionIds.contains(instruction.fromActivityId()))
                .forEach(instruction -> dispatcher.dispatch(RunActivityCommand.of(instruction.toActivityId(), process.id())));
    }

    private List<Activity> getNotCompletedActivities(ProcessExecution process,
                                                     List<String> activityDefinitionIds) {
        return activityDefinitionIds.stream()
                .flatMap(activityDefinitionId -> process.getActivitiesByDefinitionId(activityDefinitionId).stream())
                .filter(activity -> !activity.isInTerminalState())
                .toList();
    }

    private List<Activity> getActivitiesToDelete(ProcessExecution process,
                                                 List<String> fromActivitiesIds,
                                                 List<Activity> activitiesToMigrate) {
        var activitiesToMigrateIds = activitiesToMigrate.stream()
                .map(Activity::id)
                .collect(Collectors.toSet());

        return process.activities().stream()
                .filter(activity -> fromActivitiesIds.contains(activity.definitionId()))
                .filter(activity -> !activitiesToMigrateIds.contains(activity.id()))
                .toList();
    }

}
