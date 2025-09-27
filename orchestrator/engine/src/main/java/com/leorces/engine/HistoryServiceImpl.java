package com.leorces.engine;

import com.leorces.api.HistoryService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.HistoryPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryPersistence historyPersistence;

    @Override
    public PageableData<ProcessExecution> findAll(Pageable pageable) {
        log.debug("Finding all processes for pageable: {}", pageable);
        return historyPersistence.findAll(pageable);
    }

}
