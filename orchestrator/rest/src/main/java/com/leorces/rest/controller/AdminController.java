package com.leorces.rest.controller;

import com.leorces.api.AdminService;
import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.model.request.GenerateMigrationPlanRequest;
import com.leorces.rest.model.request.RunJobRequest;
import com.leorces.rest.model.response.ErrorResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.leorces.rest.constants.ApiConstants.ADMIN_ENDPOINT;
import static com.leorces.rest.constants.PaginationConstants.*;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(ADMIN_ENDPOINT)
@Tag(name = "Admin", description = "Administration operations")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Run job",
            description = "Run a specific job"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PostMapping("/jobs/run")
    public ResponseEntity<Void> runJob(@Valid @RequestBody RunJobRequest request) {
        adminService.runJob(request.type(), request.input());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get all jobs",
            description = "Retrieve a paginated list of jobs with optional filtering and sorting"
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
    @GetMapping("/jobs")
    public ResponseEntity<PageableData<Job>> findAllJobs(
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
        var result = adminService.findAllJobs(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get job by ID",
            description = "Retrieve a specific job by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_200_OK,
                    description = RESPONSE_200_OK,
                    content = @Content(schema = @Schema(implementation = Job.class))
            ),
            @ApiResponse(
                    responseCode = STATUS_404_NOT_FOUND,
                    description = RESPONSE_404_NOT_FOUND,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = STATUS_500_INTERNAL_ERROR,
                    description = RESPONSE_500_INTERNAL_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Job> findJobById(
            @Parameter(description = "The ID of the job to retrieve", required = true)
            @PathVariable("jobId") String jobId
    ) {
        return adminService.findJobById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Generate process migration plan",
            description = "Generate process migration plan"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_201_CREATED, description = RESPONSE_201_CREATED),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PostMapping("/migration")
    public ResponseEntity<ProcessMigrationPlan> generateMigrationPlan(
            @Parameter(description = "Request containing process migration params")
            @Valid @RequestBody GenerateMigrationPlanRequest request
    ) {
        var migration = ProcessMigrationPlan.builder()
                .definitionKey(request.definitionKey())
                .fromVersion(request.fromVersion())
                .toVersion(request.toVersion())
                .build();
        var result = adminService.generateMigrationPlan(migration);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
