package com.leorces.engine.admin.common.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.admin.common.command.RunJobCommand;
import com.leorces.engine.admin.common.model.JobType;
import com.leorces.engine.admin.compaction.command.CompactionCommand;
import com.leorces.engine.admin.migration.command.ProcessMigrationCommand;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionCommand;
import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunJobCommandHandler implements CommandHandler<RunJobCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(RunJobCommand command) {
        var jobType = getJobType(command);
        switch (jobType) {
            case COMPACTION:
                log.debug("Running compaction job");
                dispatcher.dispatchAsync(CompactionCommand.manual());
                break;
            case PROCESS_MIGRATION:
                log.debug("Running process migration job with input: {}", command.input());
                dispatcher.dispatch(new ProcessMigrationCommand(command.input()));
                break;
            case PROCESS_SUSPEND:
                log.debug("Running process suspend job with input: {}", command.input());
                dispatcher.dispatch(new SuspendProcessDefinitionCommand(command.input()));
                break;
            case PROCESS_RESUME:
                log.debug("Running process resume job with input: {}", command.input());
                dispatcher.dispatch(new ResumeProcessDefinitionCommand(command.input()));
                break;
            default:
                throw ExecutionException.of("Job type %s not supported".formatted(jobType));
        }
    }

    @Override
    public Class<RunJobCommand> getCommandType() {
        return RunJobCommand.class;
    }

    private JobType getJobType(RunJobCommand command) {
        try {
            return JobType.valueOf(command.jobType());
        } catch (IllegalArgumentException e) {
            throw ExecutionException.of("Job type %s not supported".formatted(command.jobType()));
        }
    }

}
