package com.leorces.persistence.postgres;

import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.HistoryPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class AdminPersistenceImpl implements AdminPersistence {

    private final ProcessPersistenceImpl processPersistence;
    private final HistoryPersistence historyPersistence;

    @Override
    @Transactional
    public int doCompaction(int batchSize) {
        var completedProcesses = processPersistence.findAllFullyCompletedForUpdate(batchSize);
        if (completedProcesses.isEmpty()) {
            log.info("No completed processes found for compaction");
            return 0;
        }

        historyPersistence.save(completedProcesses);
        return completedProcesses.size();
    }

}
