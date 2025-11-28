package com.leorces.rest.client.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static com.leorces.rest.client.constants.ApiConstants.REPOSITORY_COMPACTION_ENDPOINT;

@Slf4j
@Component
public class AdminClient {

    private final RestClient leorcesRestClient;

    public AdminClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    public void doCompaction() {
        try {
            leorcesRestClient.post()
                    .uri(REPOSITORY_COMPACTION_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't do compaction: error={}", e.getMessage());
            throw e;
        }
    }

}
