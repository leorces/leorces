package com.leorces.rest.controller;

import com.leorces.api.DefinitionService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.leorces.rest.constants.ApiConstants.DEFINITIONS;
import static com.leorces.rest.constants.PaginationConstants.*;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(DEFINITIONS)
@Tag(name = "Process Definitions", description = "Process definition management operations")
public class DefinitionController {

    private final DefinitionService definitionService;

    @Operation(
            summary = "Save process definitions",
            description = "Save a list of process definitions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_201_CREATED,
                    description = RESPONSE_201_CREATED,
                    content = @Content(schema = @Schema(implementation = ProcessDefinition.class))
            ),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PostMapping
    public ResponseEntity<List<ProcessDefinition>> save(
            @Parameter(description = "List of process definitions to save", required = true)
            @Valid @RequestBody List<ProcessDefinition> definitions
    ) {
        var result = definitionService.save(definitions);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get all process definitions",
            description = "Retrieve a paginated list of process definitions with optional filtering and sorting"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_200_OK,
                    description = RESPONSE_200_OK,
                    content = @Content(schema = @Schema(implementation = PageableData.class))
            ),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @GetMapping
    public ResponseEntity<PageableData<ProcessDefinition>> findAll(
            @Parameter(description = PAGE_DESCRIPTION)
            @RequestParam(value = PAGE_PARAM, defaultValue = DEFAULT_PAGE) int page,
            @Parameter(description = SIZE_DESCRIPTION)
            @RequestParam(value = SIZE_PARAM, defaultValue = DEFAULT_SIZE) int size,
            @Parameter(description = SORT_DESCRIPTION)
            @RequestParam(value = SORT_PARAM, defaultValue = DEFAULT_SORT_FIELD) String sortField,
            @Parameter(description = ORDER_DESCRIPTION)
            @RequestParam(value = ORDER_PARAM, defaultValue = DEFAULT_ORDER) String order,
            @Parameter(description = FILTER_DESCRIPTION)
            @RequestParam(value = FILTER_PARAM, defaultValue = DEFAULT_FILTER) String filter
    ) {
        var pageable = Pageable.builder()
                .offset((long) page * size)
                .limit(size)
                .sortByField(sortField)
                .order(Pageable.Direction.fromString(order))
                .filter(filter)
                .build();
        var result = definitionService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get process definition by ID",
            description = "Retrieve a specific process definition by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_200_OK,
                    description = RESPONSE_200_OK,
                    content = @Content(schema = @Schema(implementation = ProcessDefinition.class))
            ),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @GetMapping("/{definitionId}")
    public ResponseEntity<ProcessDefinition> findById(
            @Parameter(description = "The ID of the process definition to retrieve", required = true)
            @PathVariable("definitionId") String definitionId
    ) {
        return definitionService.findById(definitionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
