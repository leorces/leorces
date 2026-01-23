package com.leorces.engine.job.compaction.handler;

import com.leorces.engine.configuration.properties.CompactionProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.job.compaction.command.BatchCompactionCommand;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import com.leorces.engine.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompactionCommandHandler implements CommandHandler<CompactionCommand> {

    private final CompactionProperties compactionProperties;
    private final TaskExecutorService taskExecutor;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CompactionCommand command) {
        log.info("Starting compaction of completed processes");
        var futures = IntStream.range(0, compactionProperties.maxJobs())
                .mapToObj(i -> runCompactionJob())
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        log.info("Compaction finished");
    }

    @Override
    public Class<CompactionCommand> getCommandType() {
        return CompactionCommand.class;
    }

    private CompletableFuture<Void> runCompactionJob() {
        return taskExecutor.submit(() ->
                dispatcher.dispatch(BatchCompactionCommand.of(compactionProperties.batchSize()))
        );
    }

}
