package com.leorces.rest.client.client;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static com.leorces.rest.client.constants.ApiConstants.DEFINITIONS_ENDPOINT;
import static com.leorces.rest.client.constants.ApiConstants.DEFINITION_BY_ID_ENDPOINT;

@Slf4j
@Component
public class DefinitionClient {

    private static final ParameterizedTypeReference<PageableData<ProcessDefinition>> PAGEABLE_DEFINITION_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private static final ParameterizedTypeReference<List<ProcessDefinition>> DEFINITION_LIST_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient leorcesRestClient;

    public DefinitionClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    public List<ProcessDefinition> save(List<ProcessDefinition> definitions) {
        try {
            return leorcesRestClient.post()
                    .uri(DEFINITIONS_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(definitions)
                    .retrieve()
                    .body(DEFINITION_LIST_TYPE_REF);
        } catch (Exception e) {
            log.warn("Can't save definitions: definitions size={}, error={}", definitions.size(), e.getMessage());
            throw e;
        }
    }

    public PageableData<ProcessDefinition> findAll(Pageable pageable) {
        try {
            return leorcesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(DEFINITIONS_ENDPOINT)
                            .queryParam("page", pageable.offset() / pageable.limit())
                            .queryParam("size", pageable.limit())
                            .queryParam("sortField", pageable.sortByField())
                            .queryParam("order", pageable.order() != null ? pageable.order().name().toLowerCase() : "asc")
                            .queryParam("filter", pageable.filter())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(PAGEABLE_DEFINITION_TYPE_REF);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Can't find all definitions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

    public Optional<ProcessDefinition> findById(String definitionId) {
        try {
            var processDefinition = leorcesRestClient.get()
                    .uri(DEFINITION_BY_ID_ENDPOINT.formatted(definitionId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ProcessDefinition.class);
            return Optional.ofNullable(processDefinition);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Process definition not found: definitionId={}", definitionId);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Bad request for find definition by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        }
    }

}
