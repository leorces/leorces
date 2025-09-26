package com.leorces.rest.client.client;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
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
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.web.client.RestClient.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefinitionClient Tests")
class DefinitionClientTest {

    private static final String TEST_DEFINITION_ID = "test-definition-id";
    private static final List<ProcessDefinition> TEST_DEFINITIONS = List.of(
            createTestProcessDefinition("def1"),
            createTestProcessDefinition("def2")
    );

    @Mock
    private RestClient restClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    private DefinitionClient definitionClient;

    private static ProcessDefinition createTestProcessDefinition(String id) {
        // Using mock since ProcessDefinition might be complex
        var definition = mock(ProcessDefinition.class);
        when(definition.id()).thenReturn(id);
        return definition;
    }

    @BeforeEach
    void setUp() {
        definitionClient = new DefinitionClient(restClient);
    }

    @Test
    @DisplayName("Should save definitions successfully when valid definitions are provided")
    void shouldSaveDefinitionsSuccessfullyWhenValidDefinitionsAreProvided() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_DEFINITIONS)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(TEST_DEFINITIONS);

        // When
        var result = definitionClient.save(TEST_DEFINITIONS);

        // Then
        assertNotNull(result);
        assertEquals(TEST_DEFINITIONS.size(), result.size());
        verify(restClient).post();
        verify(requestBodySpec).body(TEST_DEFINITIONS);
    }

    @Test
    @DisplayName("Should return empty list when saving definitions with bad request")
    void shouldReturnEmptyListWhenSavingDefinitionsWithBadRequest() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_DEFINITIONS)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = definitionClient.save(TEST_DEFINITIONS);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw server error when saving definitions and server error occurs")
    void shouldThrowServerErrorWhenSavingDefinitionsAndServerErrorOccurs() {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(TEST_DEFINITIONS)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> definitionClient.save(TEST_DEFINITIONS));
    }

    @Test
    @DisplayName("Should find all definitions successfully when valid pageable is provided")
    void shouldFindAllDefinitionsSuccessfullyWhenValidPageableIsProvided() {
        // Given
        var pageable = createTestPageable();
        var expectedResult = new PageableData<>(TEST_DEFINITIONS, 2L);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedResult);

        // When
        var result = definitionClient.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.total());
        assertEquals(TEST_DEFINITIONS.size(), result.data().size());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return empty pageable data when finding all definitions with bad request")
    void shouldReturnEmptyPageableDataWhenFindingAllDefinitionsWithBadRequest() {
        // Given
        var pageable = createTestPageable();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = definitionClient.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.total());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("Should find definition by id successfully when valid id is provided")
    void shouldFindDefinitionByIdSuccessfullyWhenValidIdIsProvided() {
        // Given
        var expectedDefinition = createTestProcessDefinition(TEST_DEFINITION_ID);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessDefinition.class)).thenReturn(expectedDefinition);

        // When
        var result = definitionClient.findById(TEST_DEFINITION_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TEST_DEFINITION_ID, result.get().id());
        verify(restClient).get();
    }

    @Test
    @DisplayName("Should return empty optional when definition not found")
    void shouldReturnEmptyOptionalWhenDefinitionNotFound() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessDefinition.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        // When
        var result = definitionClient.findById(TEST_DEFINITION_ID);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty optional when finding definition by id with bad request")
    void shouldReturnEmptyOptionalWhenFindingDefinitionByIdWithBadRequest() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessDefinition.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

        // When
        var result = definitionClient.findById(TEST_DEFINITION_ID);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw server error when finding definition by id and server error occurs")
    void shouldThrowServerErrorWhenFindingDefinitionByIdAndServerErrorOccurs() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProcessDefinition.class))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

        // When & Then
        assertThrows(HttpServerErrorException.class,
                () -> definitionClient.findById(TEST_DEFINITION_ID));
    }

    private Pageable createTestPageable() {
        return new Pageable(0L, 10, "test-filter", "test-state");
    }

}