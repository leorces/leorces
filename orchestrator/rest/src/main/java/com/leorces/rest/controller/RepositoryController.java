package com.leorces.rest.controller;

import com.leorces.api.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leorces.rest.constants.ApiConstants.REPOSITORY_ENDPOINT;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REPOSITORY_ENDPOINT)
@Tag(name = "Repository", description = "Repository management operations")
public class RepositoryController {

    private final RepositoryService repositoryService;

    @Operation(
            summary = "Perform repository compaction",
            description = "Triggers a compaction process to optimize repository storage and performance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/compaction")
    public ResponseEntity<Void> compaction() {
        repositoryService.doCompaction();
        return ResponseEntity.noContent().build();
    }

}
