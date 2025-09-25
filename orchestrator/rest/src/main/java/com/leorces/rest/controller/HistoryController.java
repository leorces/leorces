package com.leorces.rest.controller;

import com.leorces.api.HistoryService;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.leorces.rest.constants.ApiConstants.HISTORY_ENDPOINT;
import static com.leorces.rest.constants.PaginationConstants.*;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(HISTORY_ENDPOINT)
@Tag(name = "History", description = "Process execution history operations")
public class HistoryController {

    private final HistoryService historyService;

    @Operation(
            summary = "Get process execution history",
            description = "Retrieve a paginated list of process execution history"
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
    @GetMapping()
    public ResponseEntity<PageableData<ProcessExecution>> findAll(
            @Parameter(description = PAGE_DESCRIPTION)
            @RequestParam(value = PAGE_PARAM, defaultValue = DEFAULT_PAGE) int offset,
            @Parameter(description = SIZE_DESCRIPTION)
            @RequestParam(value = SIZE_PARAM, defaultValue = DEFAULT_SIZE) int limit
    ) {
        var result = historyService.findAll(new Pageable(offset, limit));
        return ResponseEntity.ok(result);
    }

}
