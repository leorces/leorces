package com.leorces.rest.client.client;

import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.client.model.request.RunJobRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

import static com.leorces.rest.client.constants.ApiConstants.*;

@Slf4j
@Component
public class AdminClient {

    private static final ParameterizedTypeReference<PageableData<Job>> PAGEABLE_JOB_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient leorcesRestClient;

    public AdminClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    public void runJob(String jobType, Map<String, Object> inputs) {
        try {
            leorcesRestClient.post()
                    .uri(RUN_JOB_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new RunJobRequest(jobType, inputs))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't run job: error={}", e.getMessage());
            throw e;
        }
    }

    public PageableData<Job> findAllJobs(Pageable pageable) {
        try {
            return leorcesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(JOBS_ENDPOINT)
                            .queryParam("page", pageable.offset() / pageable.limit())
                            .queryParam("size", pageable.limit())
                            .queryParam("sortField", pageable.sortByField())
                            .queryParam("order", pageable.order() != null ? pageable.order().name().toLowerCase() : "asc")
                            .queryParam("filter", pageable.filter())
                            .queryParam("state", pageable.state())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(PAGEABLE_JOB_TYPE_REF);
        } catch (Exception e) {
            log.warn("Can't find all jobs: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

    public Optional<Job> findById(String jobId) {
        try {
            var job = leorcesRestClient.get()
                    .uri(JOB_BY_ID_ENDPOINT.formatted(jobId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Job.class);
            return Optional.ofNullable(job);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Job not found: jobId={}", jobId);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Bad request for find job by id: jobId={}, error={}", jobId, e.getMessage());
            throw e;
        }
    }

    public ProcessMigrationPlan generateMigrationPlan(ProcessMigrationPlan migration) {
        try {
            return leorcesRestClient.post()
                    .uri(MIGRATION_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(migration)
                    .retrieve()
                    .body(ProcessMigrationPlan.class);
        } catch (Exception e) {
            log.warn("Can't generate migration plan: error={}", e.getMessage());
            throw e;
        }
    }

}
