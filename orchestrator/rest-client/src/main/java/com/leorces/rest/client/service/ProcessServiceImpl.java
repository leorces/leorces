package com.leorces.rest.client.service;

import com.leorces.api.ProcessService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.rest.client.client.ProcessClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service("leorcesProcessService")
public class ProcessServiceImpl implements ProcessService {

    private final ProcessClient processClient;

    @Override
    public PageableData<Process> findAll(Pageable pageable) {
        return processClient.findAll(pageable);
    }

    @Override
    public Optional<ProcessExecution> findById(String processId) {
        return processClient.findById(processId);
    }

}
