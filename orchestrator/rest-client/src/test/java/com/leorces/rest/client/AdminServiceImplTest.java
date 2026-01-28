package com.leorces.rest.client;

import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.client.client.AdminClient;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceImpl Tests")
class AdminServiceImplTest {

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("Should call adminClient.runJob when running job")
    void runJob() {
        // Given
        var jobType = "TEST_JOB";
        var inputs = Map.<String, Object>of("param", "value");

        // When
        adminService.runJob(jobType, inputs);

        // Then
        verify(adminClient).runJob(jobType, inputs);
    }

    @Test
    @DisplayName("Should return all jobs from adminClient")
    void findAllJobs() {
        // Given
        var pageable = new Pageable(0, 10);
        var expectedData = new PageableData<Job>(List.of(), 0L);
        when(adminClient.findAllJobs(pageable)).thenReturn(expectedData);

        // When
        var result = adminService.findAllJobs(pageable);

        // Then
        assertThat(result).isEqualTo(expectedData);
        verify(adminClient).findAllJobs(pageable);
    }

    @Test
    @DisplayName("Should return job by id from adminClient")
    void findJobById() {
        // Given
        var jobId = "test-job-id";
        var expectedJob = Optional.of(mock(Job.class));
        when(adminClient.findById(jobId)).thenReturn(expectedJob);

        // When
        var result = adminService.findJobById(jobId);

        // Then
        assertThat(result).isEqualTo(expectedJob);
        verify(adminClient).findById(jobId);
    }

    @Test
    @DisplayName("Should return migration plan from adminClient")
    void generateMigrationPlan() {
        // Given
        var migrationPlan = ProcessMigrationPlan.builder().build();
        var expectedPlan = ProcessMigrationPlan.builder().build();
        when(adminClient.generateMigrationPlan(migrationPlan)).thenReturn(expectedPlan);

        // When
        var result = adminService.generateMigrationPlan(migrationPlan);

        // Then
        assertThat(result).isEqualTo(expectedPlan);
        verify(adminClient).generateMigrationPlan(migrationPlan);
    }

}