package com.leorces.engine;

import com.leorces.api.AdminService;
import com.leorces.engine.configuration.properties.CompactionProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.persistence.HistoryPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service("leorcesAdminService")
public class AdminServiceImpl implements AdminService {

    private final HistoryPersistence historyPersistence;
    private final ProcessPersistence processPersistence;
    private final CompactionProperties compactionProperties;
    private final TaskExecutorService taskExecutor;

    @Override
    public void doCompaction() {
        taskExecutor.execute(() -> {
            int completedProcessesCount;
            int batchSize = compactionProperties.batchSize();

            do {
                log.info("Starting compaction of completed processes");
                var completedProcesses = processPersistence.findAllFullyCompleted(batchSize);
                if (completedProcesses.isEmpty()) {
                    log.info("No completed processes found for compaction");
                    return;
                }

                historyPersistence.save(completedProcesses);
                completedProcessesCount = completedProcesses.size();
                log.info("Compacted {} completed processes", completedProcesses.size());
            } while (completedProcessesCount >= batchSize);
        });
    }

}
