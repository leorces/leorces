package com.leorces.engine;

import com.leorces.api.AdminService;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service("leorcesAdminService")
public class AdminServiceImpl implements AdminService {

    private final CommandDispatcher dispatcher;

    @Override
    public void doCompaction() {
        dispatcher.dispatchAsync(new CompactionCommand());
    }

}
