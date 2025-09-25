package com.leorces.rest.client.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RepositoryClient Tests")
class RepositoryClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private RepositoryClient repositoryClient;

    @BeforeEach
    void setUp() {
        repositoryClient = new RepositoryClient(restClient);
    }

    @Test
    @DisplayName("Should execute compaction successfully when service is available")
    void shouldExecuteCompactionSuccessfullyWhenServiceIsAvailable() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // When
        assertDoesNotThrow(() -> repositoryClient.doCompaction());

        // Then
        verify(restClient).post();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should handle bad request exception when compaction request is invalid")
    void shouldHandleBadRequestExceptionWhenCompactionRequestIsInvalid() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When & Then
        assertDoesNotThrow(() -> repositoryClient.doCompaction());
    }

    @Test
    @DisplayName("Should throw server error exception when compaction and server error occurs")
    void shouldThrowServerErrorExceptionWhenCompactionAndServerErrorOccurs() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> repositoryClient.doCompaction());
    }

    @Test
    @DisplayName("Should throw service unavailable exception when compaction and service is unavailable")
    void shouldThrowServiceUnavailableExceptionWhenCompactionAndServiceIsUnavailable() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> repositoryClient.doCompaction());
    }

    @Test
    @DisplayName("Should throw resource access exception when compaction and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenCompactionAndConnectionErrorOccurs() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> repositoryClient.doCompaction());
    }
}