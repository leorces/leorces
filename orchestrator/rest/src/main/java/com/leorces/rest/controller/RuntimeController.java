package com.leorces.rest.controller;

import com.leorces.api.RuntimeService;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.model.request.CorrelateMessageRequest;
import com.leorces.rest.model.request.ProcessModificationRequest;
import com.leorces.rest.model.request.StartProcessByIdRequest;
import com.leorces.rest.model.request.StartProcessByKeyRequest;
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

import java.util.Map;

import static com.leorces.rest.constants.ApiConstants.RUNTIME_ENDPOINT;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(RUNTIME_ENDPOINT)
@Tag(name = "Runtime", description = "Process runtime management operations")
public class RuntimeController {

    private final RuntimeService runtimeService;

    @Operation(
            summary = "Start process by key",
            description = "Start a new process using the process definition key"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_201_CREATED, description = RESPONSE_201_CREATED),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PostMapping("/processes/key")
    public ResponseEntity<Process> startProcessByKey(
            @Parameter(description = "Request containing process definition key, business key, and variables")
            @Valid @RequestBody StartProcessByKeyRequest request
    ) {
        var result = runtimeService.startProcessByKey(
                request.definitionKey(),
                request.businessKey(),
                request.variables()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
            summary = "Start process by ID",
            description = "Start a new process using the process definition ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_201_CREATED, description = RESPONSE_201_CREATED),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PostMapping("/processes")
    public ResponseEntity<Process> startProcessById(
            @Parameter(description = "Request containing process definition ID, business key, and variables")
            @Valid @RequestBody StartProcessByIdRequest request
    ) {
        var result = runtimeService.startProcessById(
                request.definitionId(),
                request.businessKey(),
                request.variables()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
            summary = "Terminate process by ID",
            description = "Terminate a process by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/processes/{processId}/terminate")
    public ResponseEntity<Process> terminateProcess(
            @Parameter(description = "The ID of the process to terminate", required = true)
            @PathVariable("processId") String processId
    ) {
        runtimeService.terminateProcess(processId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Resolve incident by process ID",
            description = "Resolve incidents in process by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/processes/{processId}/resolve-incident")
    public ResponseEntity<Void> resolveIncident(
            @Parameter(description = "The ID of the process to resolve incidents in", required = true)
            @PathVariable("processId") String processId
    ) {
        runtimeService.resolveIncident(processId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Modify process by ID",
            description = "Process modification by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/processes/{processId}/modification")
    public ResponseEntity<Void> modifyProcess(
            @Parameter(description = "The ID of the process to modify", required = true)
            @PathVariable("processId") String processId,
            @Parameter(description = "Modification request")
            @Valid @RequestBody ProcessModificationRequest request
    ) {
        runtimeService.moveExecution(processId, request.activityId(), request.targetDefinitionId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Correlate message",
            description = "Correlate a message with running processes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/correlate")
    public ResponseEntity<Void> correlateMessage(
            @Parameter(description = "Request containing message correlation details")
            @Valid @RequestBody CorrelateMessageRequest request
    ) {
        var hasBusinessKey = isNotNullOrEmpty(request.businessKey());
        var hasCorrelationKeys = isNotNullOrEmpty(request.correlationKeys());
        var hasProcessVariables = isNotNullOrEmpty(request.processVariables());

        if (hasBusinessKey && hasProcessVariables && hasCorrelationKeys) {
            runtimeService.correlateMessage(
                    request.message(),
                    request.businessKey(),
                    request.correlationKeys(),
                    request.processVariables()
            );
        } else if (hasCorrelationKeys && hasProcessVariables) {
            runtimeService.correlateMessage(
                    request.message(),
                    request.correlationKeys(),
                    request.processVariables()
            );
        } else if (hasBusinessKey && hasProcessVariables) {
            runtimeService.correlateMessage(
                    request.message(),
                    request.businessKey(),
                    request.processVariables()
            );
        } else if (hasCorrelationKeys) {
            runtimeService.correlateMessage(
                    request.message(),
                    request.correlationKeys()
            );
        } else {
            runtimeService.correlateMessage(
                    request.message(),
                    request.businessKey()
            );
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Set process variables",
            description = "Set variables for a process execution"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{executionId}/variables")
    public ResponseEntity<Void> setVariables(
            @Parameter(description = "The ID of the execution to set variables for", required = true)
            @PathVariable("executionId") String executionId,
            @Parameter(description = "Variables to set")
            @Valid @RequestBody Map<String, Object> variables
    ) {
        runtimeService.setVariables(executionId, variables);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Set local process variables",
            description = "Set local variables for a process execution"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_400_BAD_REQUEST, description = RESPONSE_400_BAD_REQUEST),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/{executionId}/variables/local")
    public ResponseEntity<Void> setVariablesLocal(
            @Parameter(description = "The ID of the execution to set local variables for", required = true)
            @PathVariable("executionId") String executionId,
            @Parameter(description = "Local variables to set")
            @Valid @RequestBody Map<String, Object> variables
    ) {
        runtimeService.setVariablesLocal(executionId, variables);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get process by filter",
            description = "Retrieve a specific process by filter"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = STATUS_200_OK,
                    description = RESPONSE_200_OK,
                    content = @Content(schema = @Schema(implementation = Process.class))
            ),
            @ApiResponse(responseCode = STATUS_404_NOT_FOUND, description = RESPONSE_404_NOT_FOUND),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PostMapping("/find/process")
    public ResponseEntity<Process> findSingleProcess(
            @Parameter(description = "Request containing search filters")
            @Valid @RequestBody ProcessFilter request

    ) {
        var process = runtimeService.findProcess(request);
        return process == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(process);
    }

    private boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isNotNullOrEmpty(Map<String, Object> map) {
        return map != null && !map.isEmpty();
    }

}
