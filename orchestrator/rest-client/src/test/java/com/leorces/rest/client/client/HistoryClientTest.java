package com.leorces.rest.client.client;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.client.RestClient.RequestHeadersSpec;
import static org.springframework.web.client.RestClient.ResponseSpec;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryClient Tests")
class HistoryClientTest {

    private static final List<ProcessExecution> TEST_PROCESS_EXECUTIONS = List.of(
            createTestProcessExecution("exec1"),
            createTestProcessExecution("exec2")
    );

    @Mock
    private RestClient restClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    private HistoryClient historyClient;

    private static ProcessExecution createTestProcessExecution(String id) {
        // Using mock since ProcessExecution might be complex
        var execution = mock(ProcessExecution.class);
        when(execution.id()).thenReturn(id);
        return execution;
    }

    @BeforeEach
    void setUp() {
        historyClient = new HistoryClient(restClient);
    }

    @Test
    @DisplayName("Should find all process executions successfully when valid pageable is provided")
    void shouldFindAllProcessExecutionsSuccessfullyWhenValidPageableIsProvided() {
        // Given
        var pageable = createTestPageable();
        var expectedResult = new PageableData<>(TEST_PROCESS_EXECUTIONS, 2L);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedResult);

        // When
        var result = historyClient.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.total());
        assertEquals(TEST_PROCESS_EXECUTIONS.size(), result.data().size());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return empty pageable data when finding all process executions with bad request")
    void shouldReturnEmptyPageableDataWhenFindingAllProcessExecutionsWithBadRequest() {
        // Given
        var pageable = createTestPageable();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = historyClient.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.total());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("Should throw server error when finding all process executions and server error occurs")
    void shouldThrowServerErrorWhenFindingAllProcessExecutionsAndServerErrorOccurs() {
        // Given
        var pageable = createTestPageable();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> historyClient.findAll(pageable));
    }

    @Test
    @DisplayName("Should throw service unavailable exception when finding all process executions and service is unavailable")
    void shouldThrowServiceUnavailableExceptionWhenFindingAllProcessExecutionsAndServiceIsUnavailable() {
        // Given
        var pageable = createTestPageable();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> historyClient.findAll(pageable));
    }

    @Test
    @DisplayName("Should throw resource access exception when finding all process executions and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenFindingAllProcessExecutionsAndConnectionErrorOccurs() {
        // Given
        var pageable = createTestPageable();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Connection error"));

        // When & Then
        assertThrows(ResourceAccessException.class,
                () -> historyClient.findAll(pageable));
    }

    private Pageable createTestPageable() {
        return new Pageable(0L, 10, "test-filter", "test-state");
    }
}