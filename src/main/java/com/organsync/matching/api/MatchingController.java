package com.organsync.matching.api;

import com.organsync.matching.dto.MatchDto;
import com.organsync.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API Controller for kidney exchange matching operations
 */
@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching", description = "Kidney Exchange Matching API")
public class MatchingController {

    private static final Logger logger = LoggerFactory.getLogger(MatchingController.class);

    @Autowired
    private MatchingService matchingService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the matching service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Matching service is running");
    }

    /**
     * Find all available matches
     */
    @GetMapping("/matches")
    @Operation(summary = "Find all matches", description = "Get all available kidney exchange matches")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved matches"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAuthority('SCOPE_match.read')")
    public ResponseEntity<List<MatchDto>> findAllMatches() {
        logger.info("Finding all available matches");

        try {
            List<MatchDto> matches = matchingService.findAllMatches();
            logger.info("Found {} matches", matches.size());
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            logger.error("Error finding matches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get match by ID
     */
    @GetMapping("/matches/{matchId}")
    @Operation(summary = "Get match by ID", description = "Retrieve a specific match by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Match found"),
        @ApiResponse(responseCode = "404", description = "Match not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAuthority('SCOPE_match.read')")
    public ResponseEntity<MatchDto> getMatchById(
            @Parameter(description = "Match ID", required = true)
            @PathVariable UUID matchId) {
        logger.info("Getting match by ID: {}", matchId);

        try {
            return matchingService.getMatchById(matchId)
                    .map(match -> ResponseEntity.ok(match))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting match by ID: {}", matchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get matches by hospital
     */
    @GetMapping("/matches/hospital/{hospitalId}")
    @Operation(summary = "Get matches by hospital", description = "Retrieve all matches for a specific hospital")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved matches"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAuthority('SCOPE_match.read')")
    public ResponseEntity<List<MatchDto>> getMatchesByHospital(
            @Parameter(description = "Hospital ID", required = true)
            @PathVariable String hospitalId) {
        logger.info("Getting matches for hospital: {}", hospitalId);

        try {
            List<MatchDto> matches = matchingService.getMatchesByHospital(hospitalId);
            logger.info("Found {} matches for hospital {}", matches.size(), hospitalId);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            logger.error("Error getting matches for hospital: {}", hospitalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update match status
     */
    @PutMapping("/matches/{matchId}/status")
    @Operation(summary = "Update match status", description = "Update the status of a specific match")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Match status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Match not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAuthority('SCOPE_match.write')")
    public ResponseEntity<MatchDto> updateMatchStatus(
            @Parameter(description = "Match ID", required = true)
            @PathVariable UUID matchId,
            @Parameter(description = "New status", required = true)
            @RequestBody @Valid StatusUpdateRequest request) {
        logger.info("Updating match {} status to {}", matchId, request.getStatus());

        try {
            MatchDto updatedMatch = matchingService.updateMatchStatus(matchId, request.getStatus());
            logger.info("Successfully updated match {} status", matchId);
            return ResponseEntity.ok(updatedMatch);
        } catch (IllegalArgumentException e) {
            logger.error("Match not found: {}", matchId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating match status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get match statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get match statistics", description = "Retrieve matching statistics and metrics")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    @PreAuthorize("hasAuthority('SCOPE_match.read')")
    public ResponseEntity<MatchingService.MatchStatistics> getMatchStatistics() {
        logger.info("Getting match statistics");

        try {
            MatchingService.MatchStatistics stats = matchingService.getMatchStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting match statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Trigger matching algorithm manually
     */
    @PostMapping("/matches/trigger")
    @Operation(summary = "Trigger matching", description = "Manually trigger the matching algorithm")
    @ApiResponse(responseCode = "200", description = "Matching algorithm triggered successfully")
    @PreAuthorize("hasAuthority('SCOPE_match.write')")
    public ResponseEntity<String> triggerMatching() {
        logger.info("Manually triggering matching algorithm");

        try {
            List<MatchDto> matches = matchingService.findAllMatches();
            logger.info("Matching algorithm completed, found {} matches", matches.size());
            return ResponseEntity.ok("Matching algorithm completed successfully. Found " + matches.size() + " matches.");
        } catch (Exception e) {
            logger.error("Error triggering matching algorithm", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error triggering matching algorithm: " + e.getMessage());
        }
    }

    /**
     * Request class for status updates
     */
    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}