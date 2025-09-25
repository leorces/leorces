package com.leorces.rest.client.client;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
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
import java.util.List;
import java.util.Optional;

import static com.leorces.rest.client.constants.ApiConstants.DEFINITIONS_ENDPOINT;
import static com.leorces.rest.client.constants.ApiConstants.DEFINITION_BY_ID_ENDPOINT;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefinitionClient {

    private static final ParameterizedTypeReference<PageableData<ProcessDefinition>> PAGEABLE_DEFINITION_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private static final ParameterizedTypeReference<List<ProcessDefinition>> DEFINITION_LIST_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    @Retry(name = "definition-save")
    @CircuitBreaker(name = "definition-save", fallbackMethod = "saveFallback")
    public List<ProcessDefinition> save(List<ProcessDefinition> definitions) {
        try {
            return restClient.post()
                    .uri(DEFINITIONS_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(definitions)
                    .retrieve()
                    .body(DEFINITION_LIST_TYPE_REF);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for save definitions: definitions size={}, error={}", definitions.size(), e.getMessage());
            return Collections.emptyList();
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during save definitions: definitions size={}, error={}", definitions.size(), e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during save definitions: definitions size={}, error={}", definitions.size(), e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during save definitions: definitions size={}, error={}", definitions.size(), e.getMessage());
            throw e;
        }
    }

    @Retry(name = "definition-findall")
    @CircuitBreaker(name = "definition-findall", fallbackMethod = "findAllFallback")
    public PageableData<ProcessDefinition> findAll(Pageable pageable) {
        try {
            return restClient.get()
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
            log.warn("Bad request for find all definitions: pageable={}, error={}", pageable, e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during find all definitions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during find all definitions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during find all definitions: pageable={}, error={}", pageable, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "definition-findbyid")
    @CircuitBreaker(name = "definition-findbyid", fallbackMethod = "findByIdFallback")
    public Optional<ProcessDefinition> findById(String definitionId) {
        try {
            var processDefinition = restClient.get()
                    .uri(DEFINITION_BY_ID_ENDPOINT.formatted(definitionId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ProcessDefinition.class);
            return Optional.ofNullable(processDefinition);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Process definition not found: definitionId={}", definitionId);
            return Optional.empty();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for find definition by id: definitionId={}, error={}", definitionId, e.getMessage());
            return Optional.empty();
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during find definition by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during find definition by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during find definition by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        }
    }

    private List<ProcessDefinition> saveFallback(List<ProcessDefinition> definitions, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for save definitions: definitions size={}, error={}", definitions.size(), e.getMessage());
            return Collections.emptyList();
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for save definitions: definitions size={}, status={}, error={}",
                    definitions.size(), clientError.getStatusCode(), e.getMessage());
            return Collections.emptyList();
        } else {
            log.error("Unexpected error for save definitions: definitions size={}", definitions.size(), e);
            return Collections.emptyList();
        }
    }

    private PageableData<ProcessDefinition> findAllFallback(Pageable pageable, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for find all definitions: pageable={}, error={}", pageable, e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for find all definitions: pageable={}, status={}, error={}",
                    pageable, clientError.getStatusCode(), e.getMessage());
            return new PageableData<>(Collections.emptyList(), 0L);
        } else {
            log.error("Unexpected error for find all definitions: pageable={}", pageable, e);
            return new PageableData<>(Collections.emptyList(), 0L);
        }
    }

    private Optional<ProcessDefinition> findByIdFallback(String definitionId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for find definition by id: definitionId={}, error={}", definitionId, e.getMessage());
            return Optional.empty();
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for find definition by id: definitionId={}, status={}, error={}",
                    definitionId, clientError.getStatusCode(), e.getMessage());
            return Optional.empty();
        } else {
            log.error("Unexpected error for find definition by id: definitionId={}", definitionId, e);
            return Optional.empty();
        }
    }

}
