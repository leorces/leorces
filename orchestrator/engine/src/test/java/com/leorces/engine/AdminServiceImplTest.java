package com.leorces.engine;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import com.leorces.engine.job.migration.command.ProcessMigrationCommand;
import com.leorces.engine.service.process.ProcessMigrationService;
import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.JobPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceImpl Tests")
class AdminServiceImplTest {

    @Mock
    private ProcessMigrationService migrationService;

    @Mock
    private JobPersistence jobPersistence;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("Should dispatch CompactionCommand.manual() when jobType is COMPACTION")
    void runJobCompaction() {
        // Given
        var jobType = "COMPACTION";
        var input = Map.<String, Object>of();

        // When
        adminService.runJob(jobType, input);

        // Then
        verify(dispatcher).dispatchAsync(any(CompactionCommand.class));
    }

    @Test
    @DisplayName("Should dispatch ProcessMigrationCommand when jobType is PROCESS_MIGRATION")
    void runJobProcessMigration() {
        // Given
        var jobType = "PROCESS_MIGRATION";
        var input = Map.<String, Object>of("key", "value");

        // When
        adminService.runJob(jobType, input);

        // Then
        verify(dispatcher).dispatch(any(ProcessMigrationCommand.class));
    }

    @Test
    @DisplayName("Should find all jobs with pagination")
    void findAllJobs() {
        // Given
        var pageable = new Pageable(0, 10);
        var expectedData = new PageableData<Job>(List.of(), 0L);
        when(jobPersistence.findAll(pageable)).thenReturn(expectedData);

        // When
        var result = adminService.findAllJobs(pageable);

        // Then
        assertThat(result).isEqualTo(expectedData);
        verify(jobPersistence).findAll(pageable);
    }

    @Test
    @DisplayName("Should find job by id")
    void findJobById() {
        // Given
        var jobId = "test-job-id";
        var expectedJob = Optional.of(mock(Job.class));
        when(jobPersistence.findJobById(jobId)).thenReturn(expectedJob);

        // When
        var result = adminService.findJobById(jobId);

        // Then
        assertThat(result).isEqualTo(expectedJob);
        verify(jobPersistence).findJobById(jobId);
    }

    @Test
    @DisplayName("Should generate migration plan")
    void generateMigrationPlan() {
        // Given
        var migrationPlan = ProcessMigrationPlan.builder().build();
        var expectedPlan = ProcessMigrationPlan.builder().build();
        when(migrationService.generateMigrationPlan(migrationPlan)).thenReturn(expectedPlan);

        // When
        var result = adminService.generateMigrationPlan(migrationPlan);

        // Then
        assertThat(result).isEqualTo(expectedPlan);
        verify(migrationService).generateMigrationPlan(migrationPlan);
    }
}
