package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.Policy;
import com.organlink.entity.Vote;
import com.organlink.entity.VoteType;
import com.organlink.security.CustomUserDetailsService;
import com.organlink.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Organization controller for policy management and voting
 * Handles organization-specific operations and governance
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * Get organization dashboard statistics
     */
    @GetMapping("/organization/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(Authentication authentication) {
        try {
            String organizationId = getOrganizationIdFromAuth(authentication);
            Map<String, Object> stats = organizationService.getDashboardStats(organizationId);
            return ResponseEntity.ok(ApiResponse.success("Organization dashboard stats retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve dashboard stats", e.getMessage()));
        }
    }

    // Policy Management Endpoints

    /**
     * Get all policies with pagination
     */
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<Page<Policy>>> getPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Policy> policies = organizationService.getPolicies(pageable);
            return ResponseEntity.ok(ApiResponse.success("Policies retrieved", policies));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve policies", e.getMessage()));
        }
    }

    /**
     * Get policy by ID
     */
    @GetMapping("/policies/{id}")
    public ResponseEntity<ApiResponse<Policy>> getPolicyById(@PathVariable Long id) {
        try {
            Optional<Policy> policy = organizationService.getPolicyById(id);
            if (policy.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Policy retrieved", policy.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve policy", e.getMessage()));
        }
    }

    /**
     * Create new policy proposal
     */
    @PostMapping("/policies")
    public ResponseEntity<ApiResponse<Policy>> createPolicy(
            @Valid @RequestBody Policy policy, 
            Authentication authentication) {
        try {
            String organizationId = getOrganizationIdFromAuth(authentication);
            Policy createdPolicy = organizationService.createPolicy(policy, organizationId);
            return ResponseEntity.ok(ApiResponse.success("Policy created successfully", createdPolicy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create policy", e.getMessage()));
        }
    }

    /**
     * Update policy
     */
    @PutMapping("/policies/{id}")
    public ResponseEntity<ApiResponse<Policy>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody Policy policy,
            Authentication authentication) {
        try {
            String organizationId = getOrganizationIdFromAuth(authentication);
            Policy updatedPolicy = organizationService.updatePolicy(id, policy, organizationId);
            return ResponseEntity.ok(ApiResponse.success("Policy updated successfully", updatedPolicy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update policy", e.getMessage()));
        }
    }

    /**
     * Vote on policy
     */
    @PostMapping("/policies/{policyId}/vote")
    public ResponseEntity<ApiResponse<Vote>> voteOnPolicy(
            @PathVariable String policyId,
            @RequestBody Map<String, String> voteRequest,
            Authentication authentication) {
        try {
            String organizationId = getOrganizationIdFromAuth(authentication);
            String voteTypeStr = voteRequest.get("vote");
            String comment = voteRequest.get("comment");
            
            // Convert vote string to VoteType enum
            VoteType voteType;
            if ("YES".equalsIgnoreCase(voteTypeStr) || "FOR".equalsIgnoreCase(voteTypeStr)) {
                voteType = VoteType.FOR;
            } else if ("NO".equalsIgnoreCase(voteTypeStr) || "AGAINST".equalsIgnoreCase(voteTypeStr)) {
                voteType = VoteType.AGAINST;
            } else if ("ABSTAIN".equalsIgnoreCase(voteTypeStr)) {
                voteType = VoteType.ABSTAIN;
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid vote type", "Vote must be YES, NO, or ABSTAIN"));
            }
            
            Vote vote = organizationService.voteOnPolicy(policyId, voteType, comment, organizationId);
            return ResponseEntity.ok(ApiResponse.success("Vote recorded successfully", vote));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to record vote", e.getMessage()));
        }
    }

    /**
     * Get policy history for organization
     */
    @GetMapping("/organization/policies/history")
    public ResponseEntity<ApiResponse<List<Policy>>> getPolicyHistory(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            String organizationId = getOrganizationIdFromAuth(authentication);
            List<Policy> policyHistory = organizationService.getPolicyHistory(organizationId, limit);
            return ResponseEntity.ok(ApiResponse.success("Policy history retrieved", policyHistory));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve policy history", e.getMessage()));
        }
    }

    /**
     * Get votes by organization
     */
    @GetMapping("/organization/votes")
    public ResponseEntity<ApiResponse<List<Vote>>> getVotesByOrganization(Authentication authentication) {
        try {
            String organizationId = getOrganizationIdFromAuth(authentication);
            List<Vote> votes = organizationService.getVotesByOrganization(organizationId);
            return ResponseEntity.ok(ApiResponse.success("Organization votes retrieved", votes));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve votes", e.getMessage()));
        }
    }

    /**
     * Get active voting policies
     */
    @GetMapping("/policies/active-voting")
    public ResponseEntity<ApiResponse<List<Policy>>> getActiveVotingPolicies() {
        try {
            List<Policy> activeVotingPolicies = organizationService.getActiveVotingPolicies();
            return ResponseEntity.ok(ApiResponse.success("Active voting policies retrieved", activeVotingPolicies));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve active voting policies", e.getMessage()));
        }
    }

    /**
     * Search policies
     */
    @GetMapping("/policies/search")
    public ResponseEntity<ApiResponse<Page<Policy>>> searchPolicies(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Policy> policies = organizationService.searchPolicies(q, pageable);
            return ResponseEntity.ok(ApiResponse.success("Policy search results", policies));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to search policies", e.getMessage()));
        }
    }

    // Helper method to extract organization ID from authentication
    private String getOrganizationIdFromAuth(Authentication authentication) {
        CustomUserDetailsService.CustomUserPrincipal principal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        // For organization users, we might need to look up the organization ID
        // This could be stored in the user entity or derived from the username
        // For now, we'll use a simple approach
        return principal.getUser().getUsername().toUpperCase();
    }
}
