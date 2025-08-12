package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.Match;
import com.organlink.service.AIMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI Matching controller for donor-patient matching
 * Handles AI-powered matching algorithms and match management
 */
@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@PreAuthorize("hasRole('HOSPITAL')")
public class AIMatchingController {

    @Autowired
    private AIMatchingService aiMatchingService;

    /**
     * Find matches for a patient using AI algorithms
     */
    @PostMapping("/find-matches/{patientId}")
    public ResponseEntity<ApiResponse<List<Match>>> findMatchesForPatient(@PathVariable Long patientId) {
        try {
            List<Match> matches = aiMatchingService.findMatchesForPatient(patientId);
            return ResponseEntity.ok(ApiResponse.success("AI matches found for patient", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to find matches", e.getMessage()));
        }
    }

    /**
     * Get match details by match ID
     */
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<ApiResponse<Match>> getMatchById(@PathVariable String matchId) {
        try {
            Optional<Match> match = aiMatchingService.getMatchById(matchId);
            if (match.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Match details retrieved", match.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve match", e.getMessage()));
        }
    }

    /**
     * Accept a match
     */
    @PostMapping("/matches/{matchId}/accept")
    public ResponseEntity<ApiResponse<Match>> acceptMatch(@PathVariable String matchId) {
        try {
            Match acceptedMatch = aiMatchingService.acceptMatch(matchId);
            return ResponseEntity.ok(ApiResponse.success("Match accepted successfully", acceptedMatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to accept match", e.getMessage()));
        }
    }

    /**
     * Reject a match
     */
    @PostMapping("/matches/{matchId}/reject")
    public ResponseEntity<ApiResponse<Match>> rejectMatch(
            @PathVariable String matchId,
            @RequestBody Map<String, String> rejectionRequest) {
        try {
            String reason = rejectionRequest.getOrDefault("reason", "No reason provided");
            Match rejectedMatch = aiMatchingService.rejectMatch(matchId, reason);
            return ResponseEntity.ok(ApiResponse.success("Match rejected", rejectedMatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to reject match", e.getMessage()));
        }
    }
}
