package com.leorces.rest.client;

import com.leorces.api.AdminService;
import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.client.client.AdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service("leorcesAdminService")
public class AdminServiceImpl implements AdminService {

    private final AdminClient adminClient;

    @Override
    public void runJob(String jobType, Map<String, Object> inputs) {
        adminClient.runJob(jobType, inputs);
    }

    @Override
    public PageableData<Job> findAllJobs(Pageable pageable) {
        return adminClient.findAllJobs(pageable);
    }

    @Override
    public Optional<Job> findJobById(String jobId) {
        return adminClient.findById(jobId);
    }

    @Override
    public ProcessMigrationPlan generateMigrationPlan(ProcessMigrationPlan migration) {
        return adminClient.generateMigrationPlan(migration);
    }

}
