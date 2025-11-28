package com.leorces.rest.client.client;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static com.leorces.rest.client.constants.ApiConstants.PROCESSES_ENDPOINT;
import static com.leorces.rest.client.constants.ApiConstants.PROCESS_BY_ID_ENDPOINT;

@Slf4j
@Component
public class ProcessClient {

    private static final ParameterizedTypeReference<PageableData<Process>> PAGEABLE_PROCESS_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient leorcesRestClient;

    public ProcessClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    public PageableData<Process> findAll(Pageable pageable) {
        try {
            return leorcesRestClient.get()
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
        } catch (Exception e) {
            log.warn("Can't find all processes: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

    public Optional<ProcessExecution> findById(String processId) {
        try {
            var processExecution = leorcesRestClient.get()
                    .uri(PROCESS_BY_ID_ENDPOINT.formatted(processId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ProcessExecution.class);
            return Optional.ofNullable(processExecution);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Process not found: processId={}", processId);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Bad request for find process by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

}
