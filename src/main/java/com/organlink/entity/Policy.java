package com.organlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Policy entity for organization governance and voting
 */
@Entity
@Table(name = "policies")
@EntityListeners(AuditingEntityListener.class)
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "policy_id", unique = true, nullable = false)
    private String policyId;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank
    @Column(name = "organ_type", nullable = false)
    private String organType;
    
    @Column(name = "policy_data", columnDefinition = "JSON")
    private String policyData; // JSON string containing policy parameters
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status = PolicyStatus.PENDING;
    
    @Column(name = "voting_start_date")
    private LocalDateTime votingStartDate;
    
    @Column(name = "voting_end_date")
    private LocalDateTime votingEndDate;
    
    @Column(name = "implementation_date")
    private LocalDateTime implementationDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // Voting Statistics
    @Column(name = "votes_for")
    private Integer votesFor = 0;
    
    @Column(name = "votes_against")
    private Integer votesAgainst = 0;
    
    @Column(name = "total_votes")
    private Integer totalVotes = 0;
    
    @Column(name = "required_votes")
    private Integer requiredVotes;
    
    @Column(name = "approval_threshold")
    private Double approvalThreshold = 0.6; // 60% by default
    
    // Blockchain Integration
    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;
    
    @Column(name = "smart_contract_address")
    private String smartContractAddress;
    
    @Column(name = "ipfs_hash")
    private String ipfsHash;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by_organization_id", nullable = false)
    private Organization proposedByOrganization;
    
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> votes = new ArrayList<>();
    
    // Constructors
    public Policy() {}
    
    public Policy(String policyId, String title, String description, String organType, Organization proposedBy) {
        this.policyId = policyId;
        this.title = title;
        this.description = description;
        this.organType = organType;
        this.proposedByOrganization = proposedBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getOrganType() { return organType; }
    public void setOrganType(String organType) { this.organType = organType; }
    
    public String getPolicyData() { return policyData; }
    public void setPolicyData(String policyData) { this.policyData = policyData; }
    
    public PolicyStatus getStatus() { return status; }
    public void setStatus(PolicyStatus status) { this.status = status; }
    
    public LocalDateTime getVotingStartDate() { return votingStartDate; }
    public void setVotingStartDate(LocalDateTime votingStartDate) { this.votingStartDate = votingStartDate; }
    
    public LocalDateTime getVotingEndDate() { return votingEndDate; }
    public void setVotingEndDate(LocalDateTime votingEndDate) { this.votingEndDate = votingEndDate; }
    
    public LocalDateTime getImplementationDate() { return implementationDate; }
    public void setImplementationDate(LocalDateTime implementationDate) { this.implementationDate = implementationDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public Integer getVotesFor() { return votesFor; }
    public void setVotesFor(Integer votesFor) { this.votesFor = votesFor; }
    
    public Integer getVotesAgainst() { return votesAgainst; }
    public void setVotesAgainst(Integer votesAgainst) { this.votesAgainst = votesAgainst; }
    
    public Integer getTotalVotes() { return totalVotes; }
    public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }
    
    public Integer getRequiredVotes() { return requiredVotes; }
    public void setRequiredVotes(Integer requiredVotes) { this.requiredVotes = requiredVotes; }
    
    public Double getApprovalThreshold() { return approvalThreshold; }
    public void setApprovalThreshold(Double approvalThreshold) { this.approvalThreshold = approvalThreshold; }
    
    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }
    
    public String getSmartContractAddress() { return smartContractAddress; }
    public void setSmartContractAddress(String smartContractAddress) { this.smartContractAddress = smartContractAddress; }
    
    public String getIpfsHash() { return ipfsHash; }
    public void setIpfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Organization getProposedByOrganization() { return proposedByOrganization; }
    public void setProposedByOrganization(Organization proposedByOrganization) { this.proposedByOrganization = proposedByOrganization; }
    
    public List<Vote> getVotes() { return votes; }
    public void setVotes(List<Vote> votes) { this.votes = votes; }
    
    // Utility methods
    public double getApprovalPercentage() {
        if (totalVotes == 0) return 0.0;
        return (double) votesFor / totalVotes * 100;
    }
    
    public boolean isVotingActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == PolicyStatus.VOTING && 
               votingStartDate != null && votingStartDate.isBefore(now) &&
               votingEndDate != null && votingEndDate.isAfter(now);
    }
    
    public boolean isApproved() {
        return getApprovalPercentage() >= (approvalThreshold * 100) && 
               totalVotes >= (requiredVotes != null ? requiredVotes : 1);
    }
    
    public void incrementVoteFor() {
        this.votesFor++;
        this.totalVotes++;
    }
    
    public void incrementVoteAgainst() {
        this.votesAgainst++;
        this.totalVotes++;
    }
}

enum PolicyStatus {
    DRAFT,
    PENDING,
    VOTING,
    APPROVED,
    REJECTED,
    IMPLEMENTED,
    EXPIRED,
    CANCELLED
}
