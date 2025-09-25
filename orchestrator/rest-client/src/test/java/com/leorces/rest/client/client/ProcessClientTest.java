package com.leorces.rest.client.client;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.web.client.RestClient.RequestHeadersSpec;
import static org.springframework.web.client.RestClient.ResponseSpec;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessClient Tests")
class ProcessClientTest {

    private static final String TEST_PROCESS_ID = "test-process-id";
    private static final List<Process> TEST_PROCESSES = List.of(
            createTestProcess("process1"),
            createTestProcess("process2")
    );

    @Mock
    private RestClient restClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    private ProcessClient processClient;

    private static Process createTestProcess(String id) {
        // Using mock since Process might be complex
        var process = mock(Process.class);
        when(process.id()).thenReturn(id);
        return process;
    }

    private static ProcessExecution createTestProcessExecution(String id) {
        // Using mock since ProcessExecution might be complex
        var execution = mock(ProcessExecution.class);
        when(execution.id()).thenReturn(id);
        return execution;
    }

    @BeforeEach
    void setUp() {
        processClient = new ProcessClient(restClient);
    }

    @Test
    @DisplayName("Should find all processes successfully when valid pageable is provided")
    void shouldFindAllProcessesSuccessfullyWhenValidPageableIsProvided() {
        // Given
        var pageable = createTestPageable();
        var expectedResult = new PageableData<>(TEST_PROCESSES, 2L);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedResult);

        // When
        var result = processClient.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.total());
        assertEquals(TEST_PROCESSES.size(), result.data().size());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return empty pageable data when finding all processes with bad request")
    void shouldReturnEmptyPageableDataWhenFindingAllProcessesWithBadRequest() {
        // Given
        var pageable = createTestPageable();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = processClient.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.total());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("Should throw server error when finding all processes and server error occurs")
    void shouldThrowServerErrorWhenFindingAllProcessesAndServerErrorOccurs() {
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
                () -> processClient.findAll(pageable));
    }

    @Test
    @DisplayName("Should find process execution by id successfully when valid id is provided")
    void shouldFindProcessExecutionByIdSuccessfullyWhenValidIdIsProvided() {
        // Given
        var expectedExecution = createTestProcessExecution(TEST_PROCESS_ID);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessExecution.class)).thenReturn(expectedExecution);

        // When
        var result = processClient.findById(TEST_PROCESS_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TEST_PROCESS_ID, result.get().id());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return empty optional when process not found")
    void shouldReturnEmptyOptionalWhenProcessNotFound() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessExecution.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        // When
        var result = processClient.findById(TEST_PROCESS_ID);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty optional when finding process by id with bad request")
    void shouldReturnEmptyOptionalWhenFindingProcessByIdWithBadRequest() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessExecution.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = processClient.findById(TEST_PROCESS_ID);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw server error when finding process by id and server error occurs")
    void shouldThrowServerErrorWhenFindingProcessByIdAndServerErrorOccurs() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessExecution.class))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> processClient.findById(TEST_PROCESS_ID));
    }

    @Test
    @DisplayName("Should throw service unavailable exception when finding processes and service is unavailable")
    void shouldThrowServiceUnavailableExceptionWhenFindingProcessesAndServiceIsUnavailable() {
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
                () -> processClient.findAll(pageable));
    }

    @Test
    @DisplayName("Should throw resource access exception when finding processes and connection error occurs")
    void shouldThrowResourceAccessExceptionWhenFindingProcessesAndConnectionErrorOccurs() {
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
                () -> processClient.findAll(pageable));
    }

    private Pageable createTestPageable() {
        return new Pageable(0L, 10, "test-filter", "test-state");
    }
}