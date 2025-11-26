package com.leorces.rest.client.client;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Collections;

import static com.leorces.rest.client.constants.ApiConstants.HISTORY_ENDPOINT;

@Slf4j
@Component
public class HistoryClient {

    private static final ParameterizedTypeReference<PageableData<ProcessExecution>> PAGEABLE_PROCESS_EXECUTION_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient leorcesRestClient;

    public HistoryClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    @Retry(name = "history-findall")
    @CircuitBreaker(name = "history-findall", fallbackMethod = "findAllFallback")
    public PageableData<ProcessExecution> findAll(Pageable pageable) {
        try {
            return leorcesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(HISTORY_ENDPOINT)
                            .queryParam("page", pageable.offset() / pageable.limit())
                            .queryParam("size", pageable.limit())
                            .queryParam("sortField", pageable.sortByField())
                            .queryParam("order", pageable.order() != null ? pageable.order().name().toLowerCase() : "asc")
                            .queryParam("filter", pageable.filter())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(PAGEABLE_PROCESS_EXECUTION_TYPE_REF);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for find all process executions: pageable={}, error={}", pageable, e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during find all process executions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during find all process executions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during find all process executions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

    private PageableData<ProcessExecution> findAllFallback(Pageable pageable, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for find all process executions: pageable={}, error={}", pageable, e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for find all process executions: pageable={}, status={}, error={}",
                    pageable, clientError.getStatusCode(), e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } else {
            log.error("Unexpected error for find all process executions: pageable={}", pageable, e);
            return new PageableData<>(Collections.emptyList(), 0L);
        }
    }

}
