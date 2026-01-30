package com.leorces.engine;

import com.leorces.api.AdminService;
import com.leorces.engine.admin.common.command.RunJobCommand;
import com.leorces.engine.admin.migration.command.GenerateProcessMigrationPlanCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.JobPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service("leorcesAdminService")
public class AdminServiceImpl implements AdminService {

    private final JobPersistence jobPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void runJob(String jobType, Map<String, Object> input) {
        log.debug("Running job: {} with input: {}", jobType, input);
        dispatcher.dispatch(RunJobCommand.of(jobType, input));
    }

    @Override
    public PageableData<Job> findAllJobs(Pageable pageable) {
        log.debug("Finding all jobs for pageable: {}", pageable);
        return jobPersistence.findAll(pageable);
    }

    @Override
    public Optional<Job> findJobById(String jobId) {
        log.debug("Finding job by id: {}", jobId);
        return jobPersistence.findJobById(jobId);
    }

    @Override
    public ProcessMigrationPlan generateMigrationPlan(ProcessMigrationPlan migration) {
        log.debug("Generating migration plan for migration: {}", migration);
        return dispatcher.execute(new GenerateProcessMigrationPlanCommand(migration));
    }

}
