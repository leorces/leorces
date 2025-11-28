package com.leorces.rest.client.client;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        } catch (Exception e) {
            log.warn("Can't find all process executions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

}
