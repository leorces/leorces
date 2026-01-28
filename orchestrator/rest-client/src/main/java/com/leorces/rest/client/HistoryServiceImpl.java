package com.leorces.rest.client;

import com.leorces.api.HistoryService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.rest.client.client.HistoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("leorcesHistoryService")
public class HistoryServiceImpl implements HistoryService {

    private final HistoryClient historyClient;

    @Override
    public PageableData<ProcessExecution> findAll(Pageable pageable) {
        return historyClient.findAll(pageable);
    }

}
