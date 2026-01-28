package com.leorces.rest.client.client;

import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.client.model.request.RunJobRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.leorces.rest.client.constants.ApiConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminClient Tests")
class AdminClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private AdminClient adminClient;

    @Test
    @DisplayName("Should run job successfully")
    void runJobSuccess() {
        // Given
        var jobType = "testJob";
        var inputs = Map.<String, Object>of("key", "value");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(RUN_JOB_ENDPOINT)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(RunJobRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        adminClient.runJob(jobType, inputs);

        // Then
        verify(restClient).post();
        verify(requestBodyUriSpec).uri(RUN_JOB_ENDPOINT);
        verify(requestBodySpec).body(new RunJobRequest(jobType, inputs));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should throw exception when run job fails")
    void runJobFailure() {
        // Given
        var jobType = "testJob";
        var inputs = Map.<String, Object>of("key", "value");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(RUN_JOB_ENDPOINT)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("error"));

        // When & Then
        assertThatThrownBy(() -> adminClient.runJob(jobType, inputs))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should find all jobs successfully")
    @SuppressWarnings("unchecked")
    void findAllJobsSuccess() {
        // Given
        var pageable = new Pageable(0L, 10, "filter", "state", "createdAt", Pageable.Direction.DESC);
        var expectedData = new PageableData<Job>(List.of(), 0L);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedData);

        // When
        var result = adminClient.findAllJobs(pageable);

        // Then
        assertThat(result).isEqualTo(expectedData);
        verify(restClient).get();
        verify(requestHeadersSpec).retrieve();
    }

    @Test
    @DisplayName("Should find job by id successfully")
    void findByIdSuccess() {
        // Given
        var jobId = "job1";
        var expectedJob = Job.builder().id(jobId).build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(JOB_BY_ID_ENDPOINT.formatted(jobId))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Job.class)).thenReturn(expectedJob);

        // When
        var result = adminClient.findById(jobId);

        // Then
        assertThat(result).isPresent().contains(expectedJob);
        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(JOB_BY_ID_ENDPOINT.formatted(jobId));
    }

    @Test
    @DisplayName("Should return empty optional when job not found")
    void findByIdNotFound() {
        // Given
        var jobId = "job1";

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Job.class)).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        // When
        var result = adminClient.findById(jobId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should generate migration plan successfully")
    void generateMigrationPlanSuccess() {
        // Given
        var plan = ProcessMigrationPlan.builder().build();
        var expectedPlan = ProcessMigrationPlan.builder().build();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(MIGRATION_ENDPOINT)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(plan)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessMigrationPlan.class)).thenReturn(expectedPlan);

        // When
        var result = adminClient.generateMigrationPlan(plan);

        // Then
        assertThat(result).isEqualTo(expectedPlan);
        verify(restClient).post();
        verify(requestBodyUriSpec).uri(MIGRATION_ENDPOINT);
    }

    @Test
    @DisplayName("Should throw exception when find all jobs fails")
    void findAllJobsFailure() {
        // Given
        var pageable = new Pageable(0L, 10);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("error"));

        // When & Then
        assertThatThrownBy(() -> adminClient.findAllJobs(pageable))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when find job by id fails")
    void findByIdFailure() {
        // Given
        var jobId = "job1";

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("error"));

        // When & Then
        assertThatThrownBy(() -> adminClient.findById(jobId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when generate migration plan fails")
    void generateMigrationPlanFailure() {
        // Given
        var plan = ProcessMigrationPlan.builder().build();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("error"));

        // When & Then
        assertThatThrownBy(() -> adminClient.generateMigrationPlan(plan))
                .isInstanceOf(RuntimeException.class);
    }
}