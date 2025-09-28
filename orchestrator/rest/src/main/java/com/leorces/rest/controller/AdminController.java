package com.leorces.rest.controller;

import com.leorces.api.AdminService;
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

import static com.leorces.rest.constants.ApiConstants.ADMIN_ENDPOINT;
import static com.leorces.rest.constants.SwaggerConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(ADMIN_ENDPOINT)
@Tag(name = "Admin", description = "Administration operations")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Perform repository compaction",
            description = "Triggers a compaction process to optimize repository storage and performance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = STATUS_204_NO_CONTENT, description = RESPONSE_204_NO_CONTENT),
            @ApiResponse(responseCode = STATUS_500_INTERNAL_ERROR, description = RESPONSE_500_INTERNAL_ERROR)
    })
    @PutMapping("/repository/compaction")
    public ResponseEntity<Void> compaction() {
        adminService.doCompaction();
        return ResponseEntity.noContent().build();
    }

}
