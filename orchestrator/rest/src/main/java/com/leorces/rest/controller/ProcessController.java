package com.leorces.rest.controller;

import com.leorces.api.ProcessService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.leorces.rest.constants.ApiConstants.PROCESSES_ENDPOINT;
import static com.leorces.rest.constants.PaginationConstants.*;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(PROCESSES_ENDPOINT)
@Tag(name = "Processes", description = "Process management operations")
public class ProcessController {

    private final ProcessService processService;

    @Operation(
            summary = "Get all processes",
            description = "Retrieve a paginated list of processes with optional filtering and sorting"
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
    public ResponseEntity<PageableData<Process>> findAll(
            @Parameter(description = PAGE_DESCRIPTION)
            @RequestParam(value = PAGE_PARAM, defaultValue = DEFAULT_PAGE) int page,
            @Parameter(description = SIZE_DESCRIPTION)
            @RequestParam(value = SIZE_PARAM, defaultValue = DEFAULT_SIZE) int size,
            @Parameter(description = SORT_DESCRIPTION)
            @RequestParam(value = SORT_PARAM, defaultValue = DEFAULT_SORT_FIELD) String sortField,
            @Parameter(description = ORDER_DESCRIPTION)
            @RequestParam(value = ORDER_PARAM, defaultValue = DEFAULT_ORDER) String order,
            @Parameter(description = FILTER_DESCRIPTION)
            @RequestParam(value = FILTER_PARAM, defaultValue = DEFAULT_FILTER) String filter,
            @Parameter(description = STATE_DESCRIPTION)
            @RequestParam(value = STATE_PARAM, defaultValue = DEFAULT_STATE) String state
    ) {
        var pageable = Pageable.builder()
                .offset((long) page * size)
                .limit(size)
                .sortByField(sortField)
                .order(Pageable.Direction.fromString(order))
                .filter(filter)
                .state(state)
                .build();
        var result = processService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get process by ID",
            description = "Retrieve a specific process by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_200_OK,
                    description = RESPONSE_200_OK,
                    content = @Content(schema = @Schema(implementation = ProcessExecution.class))
            ),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @GetMapping("/{processId}")
    public ResponseEntity<ProcessExecution> findById(
            @Parameter(description = "The ID of the process to retrieve", required = true)
            @PathVariable("processId") String processId
    ) {
        return processService.findById(processId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
