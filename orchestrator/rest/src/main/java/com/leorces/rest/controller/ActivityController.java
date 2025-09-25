package com.leorces.rest.controller;

import com.leorces.api.ActivityService;
import com.leorces.model.runtime.activity.Activity;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.leorces.rest.constants.ApiConstants.ACTIVITIES_ENDPOINT;
import static com.leorces.rest.constants.PaginationConstants.DEFAULT_SIZE;
import static com.leorces.rest.constants.PaginationConstants.SIZE_PARAM;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(ACTIVITIES_ENDPOINT)
@Tag(name = "Activities", description = "Activity management operations")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(
            summary = "Run activity",
            description = "Start execution of an activity by process ID and activity definition ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{processId}/{activityDefinitionId}/run")
    public ResponseEntity<Void> run(
            @Parameter(description = "The ID of the process", required = true)
            @PathVariable("processId") String processId,
            @Parameter(description = "The ID of the activity definition", required = true)
            @PathVariable("activityDefinitionId") String activityDefinitionId
    ) {
        activityService.run(activityDefinitionId, processId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Complete activity",
            description = "Complete an activity by its ID with optional variables"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{activityId}/complete")
    public ResponseEntity<Void> complete(
            @Parameter(description = "The ID of the activity to complete", required = true)
            @PathVariable("activityId") String activityId,
            @Parameter(description = "Optional variables to set when completing the activity")
            @Valid @RequestBody(required = false) Map<String, Object> variables
    ) {
        activityService.complete(activityId, Objects.requireNonNullElse(variables, Collections.emptyMap()));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Fail activity",
            description = "Mark an activity as failed by its ID with optional variables"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{activityId}/fail")
    public ResponseEntity<Void> fail(
            @Parameter(description = "The ID of the activity to fail", required = true)
            @PathVariable("activityId") String activityId,
            @Parameter(description = "Optional variables to set when failing the activity")
            @Valid @RequestBody(required = false) Map<String, Object> variables
    ) {
        activityService.fail(activityId, Objects.requireNonNullElse(variables, Collections.emptyMap()));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Terminate activity",
            description = "Terminate an activity by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{activityId}/terminate")
    public ResponseEntity<Void> terminate(
            @Parameter(description = "The ID of the activity to terminate", required = true)
            @PathVariable("activityId") String activityId
    ) {
        activityService.terminate(activityId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Retry activity",
            description = "Retry a failed activity by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{activityId}/retry")
    public ResponseEntity<Void> retry(
            @Parameter(description = "The ID of the activity to retry", required = true)
            @PathVariable("activityId") String activityId
    ) {
        activityService.retry(activityId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Poll activities",
            description = "Poll for available activities by process definition key and topic"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_200_OK,
                    description = RESPONSE_200_OK,
                    content = @Content(schema = @Schema(implementation = Activity.class))
            ),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @GetMapping("/poll/{processDefinitionKey}/{topic}")
    public ResponseEntity<List<Activity>> poll(
            @Parameter(description = "The process definition key to poll activities for", required = true)
            @PathVariable("processDefinitionKey") String processDefinitionKey,
            @Parameter(description = "The topic to poll activities for", required = true)
            @PathVariable("topic") String topic,
            @Parameter(description = SIZE_DESCRIPTION)
            @RequestParam(value = SIZE_PARAM, defaultValue = DEFAULT_SIZE) int size
    ) {
        var result = activityService.poll(topic, processDefinitionKey, size);
        return ResponseEntity.ok(result);
    }
}
