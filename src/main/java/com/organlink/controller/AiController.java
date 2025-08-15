package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.Match;
import com.organlink.service.AIMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI Controller for managing model training and matching processes.
 */
@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class AiController {

    @Autowired
    private AIMatchingService aiMatchingService;

    /**
     * Manually trigger the AI model training process.
     * Accessible only by Admins.
     */
    @PostMapping("/train")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> trainModels() {
        try {
            aiMatchingService.trainModels();
            return ResponseEntity.ok(ApiResponse.success("AI model training initiated successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to start AI model training.", e.getMessage()));
        }
    }

    /**
     * Find the best donor matches for a given patient.
     * Accessible by Hospitals.
     */
    @GetMapping("/matches/{patientId}")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<ApiResponse<List<Match>>> findMatches(@PathVariable Long patientId) {
        try {
            List<Match> matches = aiMatchingService.findBestMatchesForPatient(patientId);
            return ResponseEntity.ok(ApiResponse.success("Successfully retrieved potential matches.", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to find matches for patient: " + patientId, e.getMessage()));
        }
    }
}
