package com.organlink.service;

import com.organlink.entity.*;
import com.organlink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Organization service for policy management and voting
 */
@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private BlockchainService blockchainService;

    /**
     * Get organization dashboard statistics
     */
    public Map<String, Object> getDashboardStats(String organizationId) {
        Optional<Organization> orgOpt = organizationRepository.findByOrganizationId(organizationId);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found: " + organizationId);
        }

        Organization organization = orgOpt.get();
        Map<String, Object> stats = new HashMap<>();
        
        // Policy statistics
        stats.put("activePolicies", policyRepository.countByStatus(PolicyStatus.IMPLEMENTED));
        stats.put("myProposals", policyRepository.findByProposedByOrganizationId(organization.getId()).size());
        stats.put("pendingVotes", getPendingVotesCount(organization.getId()));
        stats.put("totalVotesCast", voteRepository.findByOrganizationId(organization.getId()).size());
        
        // Voting statistics
        long totalVotesFor = voteRepository.findByOrganizationId(organization.getId()).stream()
                .mapToLong(votes -> votes.stream().filter(v -> v.getVoteType() == VoteType.FOR).count())
                .sum();
        long totalVotes = voteRepository.findByOrganizationId(organization.getId()).size();
        double approvalRate = totalVotes > 0 ? (double) totalVotesFor / totalVotes * 100 : 0;
        stats.put("approvalRate", Math.round(approvalRate));
        
        return stats;
    }

    /**
     * Get policies with pagination
     */
    public Page<Policy> getPolicies(Pageable pageable) {
        return policyRepository.findAllOrderByCreatedAtDesc(pageable);
    }

    /**
     * Get policy by ID
     */
    public Optional<Policy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }

    /**
     * Get policy by policy ID
     */
    public Optional<Policy> getPolicyByPolicyId(String policyId) {
        return policyRepository.findByPolicyId(policyId);
    }

    /**
     * Create new policy proposal
     */
    public Policy createPolicy(Policy policy, String organizationId) {
        Optional<Organization> orgOpt = organizationRepository.findByOrganizationId(organizationId);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found: " + organizationId);
        }

        Organization organization = orgOpt.get();
        
        // Generate unique policy ID
        policy.setPolicyId(generatePolicyId());
        policy.setProposedByOrganization(organization);
        policy.setStatus(PolicyStatus.PENDING);
        
        // Set voting period (30 days from now)
        policy.setVotingStartDate(LocalDateTime.now());
        policy.setVotingEndDate(LocalDateTime.now().plusDays(30));
        
        // Calculate required votes (majority of active organizations)
        long totalOrganizations = organizationRepository.countByStatus(OrganizationStatus.ACTIVE);
        policy.setRequiredVotes((int) Math.ceil(totalOrganizations / 2.0));
        
        Policy savedPolicy = policyRepository.save(policy);
        
        // Record on blockchain (async)
        try {
            blockchainService.recordPolicyCreation(savedPolicy);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to record policy on blockchain: " + e.getMessage());
        }
        
        return savedPolicy;
    }

    /**
     * Update policy
     */
    public Policy updatePolicy(Long id, Policy policyDetails, String organizationId) {
        Optional<Policy> policyOpt = policyRepository.findById(id);
        if (policyOpt.isEmpty()) {
            throw new RuntimeException("Policy not found");
        }

        Policy policy = policyOpt.get();
        
        // Check if organization owns this policy
        if (!policy.getProposedByOrganization().getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: You can only update your own policies");
        }
        
        // Only allow updates if policy is still in DRAFT or PENDING status
        if (policy.getStatus() != PolicyStatus.DRAFT && policy.getStatus() != PolicyStatus.PENDING) {
            throw new RuntimeException("Cannot update policy in current status: " + policy.getStatus());
        }
        
        // Update fields
        policy.setTitle(policyDetails.getTitle());
        policy.setDescription(policyDetails.getDescription());
        policy.setOrganType(policyDetails.getOrganType());
        policy.setPolicyData(policyDetails.getPolicyData());
        
        return policyRepository.save(policy);
    }

    /**
     * Vote on policy
     */
    public Vote voteOnPolicy(String policyId, VoteType voteType, String comment, String organizationId) {
        Optional<Policy> policyOpt = policyRepository.findByPolicyId(policyId);
        if (policyOpt.isEmpty()) {
            throw new RuntimeException("Policy not found: " + policyId);
        }

        Optional<Organization> orgOpt = organizationRepository.findByOrganizationId(organizationId);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found: " + organizationId);
        }

        Policy policy = policyOpt.get();
        Organization organization = orgOpt.get();
        
        // Check if policy is in voting status
        if (!policy.isVotingActive()) {
            throw new RuntimeException("Policy is not currently open for voting");
        }
        
        // Check if organization has already voted
        if (voteRepository.existsByPolicyIdAndOrganizationId(policy.getId(), organization.getId())) {
            throw new RuntimeException("Organization has already voted on this policy");
        }
        
        // Create vote
        Vote vote = new Vote();
        vote.setPolicy(policy);
        vote.setOrganization(organization);
        vote.setVoteType(voteType);
        vote.setComment(comment);
        vote.setVotingPower(organization.getVotingPower());
        
        Vote savedVote = voteRepository.save(vote);
        
        // Update policy vote counts
        if (voteType == VoteType.FOR) {
            policy.incrementVoteFor();
        } else if (voteType == VoteType.AGAINST) {
            policy.incrementVoteAgainst();
        }
        
        // Check if policy should be approved or rejected
        updatePolicyStatus(policy);
        policyRepository.save(policy);
        
        // Record vote on blockchain (async)
        try {
            blockchainService.recordVote(savedVote);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to record vote on blockchain: " + e.getMessage());
        }
        
        return savedVote;
    }

    /**
     * Get policy history for organization
     */
    public List<Policy> getPolicyHistory(String organizationId, int limit) {
        Optional<Organization> orgOpt = organizationRepository.findByOrganizationId(organizationId);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found: " + organizationId);
        }

        Organization organization = orgOpt.get();
        List<Policy> allPolicies = policyRepository.findByProposedByOrganizationId(organization.getId());
        
        // Return limited results
        return allPolicies.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    /**
     * Get votes by organization
     */
    public List<Vote> getVotesByOrganization(String organizationId) {
        Optional<Organization> orgOpt = organizationRepository.findByOrganizationId(organizationId);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found: " + organizationId);
        }

        Organization organization = orgOpt.get();
        return voteRepository.findByOrganizationIdOrderByCreatedAtDesc(organization.getId());
    }

    /**
     * Get active voting policies
     */
    public List<Policy> getActiveVotingPolicies() {
        return policyRepository.findActiveVotingPolicies(LocalDateTime.now());
    }

    /**
     * Search policies
     */
    public Page<Policy> searchPolicies(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return policyRepository.findAllOrderByCreatedAtDesc(pageable);
        }
        return policyRepository.findByTitleContaining(searchTerm.trim(), pageable);
    }

    // Helper methods
    private String generatePolicyId() {
        long count = policyRepository.count() + 1;
        return "POL-" + java.time.Year.now().getValue() + "-" + String.format("%03d", count);
    }

    private long getPendingVotesCount(Long organizationId) {
        List<Policy> activeVotingPolicies = policyRepository.findActiveVotingPolicies(LocalDateTime.now());
        return activeVotingPolicies.stream()
                .filter(policy -> !voteRepository.existsByPolicyIdAndOrganizationId(policy.getId(), organizationId))
                .count();
    }

    private void updatePolicyStatus(Policy policy) {
        // Check if voting period has ended
        if (policy.getVotingEndDate().isBefore(LocalDateTime.now())) {
            if (policy.isApproved()) {
                policy.setStatus(PolicyStatus.APPROVED);
                policy.setImplementationDate(LocalDateTime.now().plusDays(7)); // Implement in 7 days
            } else {
                policy.setStatus(PolicyStatus.REJECTED);
            }
        }
        // Check if required votes reached before deadline
        else if (policy.getTotalVotes() >= policy.getRequiredVotes()) {
            if (policy.isApproved()) {
                policy.setStatus(PolicyStatus.APPROVED);
                policy.setImplementationDate(LocalDateTime.now().plusDays(7));
            } else {
                policy.setStatus(PolicyStatus.REJECTED);
            }
        }
        // Policy is still in voting
        else {
            policy.setStatus(PolicyStatus.VOTING);
        }
    }
}
