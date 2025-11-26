package com.leorces.rest.client.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static com.leorces.rest.client.constants.ApiConstants.REPOSITORY_COMPACTION_ENDPOINT;

@Slf4j
@Component
public class AdminClient {

    private final RestClient leorcesRestClient;

    public AdminClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    @Retry(name = "repository-compaction")
    @CircuitBreaker(name = "repository-compaction", fallbackMethod = "doCompactionFallback")
    public void doCompaction() {
        try {
            leorcesRestClient.post()
                    .uri(REPOSITORY_COMPACTION_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Repository compaction completed successfully");
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for repository compaction: error={}", e.getMessage());
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during repository compaction: error={}", e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during repository compaction: error={}", e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during repository compaction: error={}", e.getMessage());
            throw e;
        }
    }

    private void doCompactionFallback(Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for repository compaction: error={}", e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for repository compaction: status={}, error={}",
                    clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for repository compaction", e);
        }
    }

}
