package com.leorces.rest.client.client;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Optional;

import static com.leorces.rest.client.constants.ApiConstants.PROCESSES_ENDPOINT;
import static com.leorces.rest.client.constants.ApiConstants.PROCESS_BY_ID_ENDPOINT;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessClient {

    private static final ParameterizedTypeReference<PageableData<Process>> PAGEABLE_PROCESS_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    @Retry(name = "process-findall")
    @CircuitBreaker(name = "process-findall", fallbackMethod = "findAllFallback")
    public PageableData<Process> findAll(Pageable pageable) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(PROCESSES_ENDPOINT)
                            .queryParam("page", pageable.offset() / pageable.limit())
                            .queryParam("size", pageable.limit())
                            .queryParam("sortField", pageable.sortByField())
                            .queryParam("order", pageable.order() != null ? pageable.order().name().toLowerCase() : "asc")
                            .queryParam("filter", pageable.filter())
                            .queryParam("state", pageable.state())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(PAGEABLE_PROCESS_TYPE_REF);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for find all processes: pageable={}, error={}", pageable, e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during find all processes: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during find all processes: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during find all processes: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "process-findbyid")
    @CircuitBreaker(name = "process-findbyid", fallbackMethod = "findByIdFallback")
    public Optional<ProcessExecution> findById(String processId) {
        try {
            var processExecution = restClient.get()
                    .uri(PROCESS_BY_ID_ENDPOINT.formatted(processId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ProcessExecution.class);
            return Optional.ofNullable(processExecution);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Process not found: processId={}", processId);
            return Optional.empty();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for find process by id: processId={}, error={}", processId, e.getMessage());
            return Optional.empty();
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during find process by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during find process by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during find process by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    private PageableData<Process> findAllFallback(Pageable pageable, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for find all processes: pageable={}, error={}", pageable, e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for find all processes: pageable={}, status={}, error={}",
                    pageable, clientError.getStatusCode(), e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } else {
            log.error("Unexpected error for find all processes: pageable={}", pageable, e);
            return new PageableData<>(Collections.emptyList(), 0L);
        }
    }

    private Optional<ProcessExecution> findByIdFallback(String processId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for find process by id: processId={}, error={}", processId, e.getMessage());
            return Optional.empty();
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for find process by id: processId={}, status={}, error={}",
                    processId, clientError.getStatusCode(), e.getMessage());
            return Optional.empty();
        } else {
            log.error("Unexpected error for find process by id: processId={}", processId, e);
            return Optional.empty();
        }
    }

}
