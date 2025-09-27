package com.leorces.engine;

import com.leorces.api.ProcessService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.ProcessPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final ProcessPersistence processPersistence;

    @Override
    public PageableData<Process> findAll(Pageable pageable) {
        log.debug("Finding all processes for pageable: {}", pageable);
        return processPersistence.findAll(pageable);
    }

    @Override
    public Optional<ProcessExecution> findById(String processId) {
        log.debug("Finding process by id: {}", processId);
        return processPersistence.findExecutionById(processId);
    }

}
