package com.organlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Match entity for AI-powered donor-patient matching
 */
@Entity
@Table(name = "matches")
@EntityListeners(AuditingEntityListener.class)
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "match_id", unique = true, nullable = false)
    private String matchId;
    
    @NotNull
    @Column(name = "compatibility_score", nullable = false)
    private Double compatibilityScore;
    
    @Column(name = "distance_km")
    private Double distanceKm;
    
    @Column(name = "urgency_factor")
    private Double urgencyFactor;
    
    @Column(name = "policy_weighted_score")
    private Double policyWeightedScore;
    
    @Column(name = "final_score")
    private Double finalScore;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.PENDING;
    
    @Column(name = "ai_model_version")
    private String aiModelVersion;
    
    @Column(name = "matching_algorithm")
    private String matchingAlgorithm;
    
    @Column(name = "match_details", columnDefinition = "JSON")
    private String matchDetails; // JSON containing detailed matching criteria
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "accepted_date")
    private LocalDateTime acceptedDate;
    
    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    // Blockchain Integration
    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    // Constructors
    public Match() {}
    
    public Match(String matchId, Donor donor, Patient patient, Double compatibilityScore) {
        this.matchId = matchId;
        this.donor = donor;
        this.patient = patient;
        this.compatibilityScore = compatibilityScore;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    
    public Double getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Double compatibilityScore) { this.compatibilityScore = compatibilityScore; }
    
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    
    public Double getUrgencyFactor() { return urgencyFactor; }
    public void setUrgencyFactor(Double urgencyFactor) { this.urgencyFactor = urgencyFactor; }
    
    public Double getPolicyWeightedScore() { return policyWeightedScore; }
    public void setPolicyWeightedScore(Double policyWeightedScore) { this.policyWeightedScore = policyWeightedScore; }
    
    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
    
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    
    public String getAiModelVersion() { return aiModelVersion; }
    public void setAiModelVersion(String aiModelVersion) { this.aiModelVersion = aiModelVersion; }
    
    public String getMatchingAlgorithm() { return matchingAlgorithm; }
    public void setMatchingAlgorithm(String matchingAlgorithm) { this.matchingAlgorithm = matchingAlgorithm; }
    
    public String getMatchDetails() { return matchDetails; }
    public void setMatchDetails(String matchDetails) { this.matchDetails = matchDetails; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getAcceptedDate() { return acceptedDate; }
    public void setAcceptedDate(LocalDateTime acceptedDate) { this.acceptedDate = acceptedDate; }
    
    public LocalDateTime getRejectedDate() { return rejectedDate; }
    public void setRejectedDate(LocalDateTime rejectedDate) { this.rejectedDate = rejectedDate; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }
    
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    
    // Utility methods
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
    
    public boolean isActive() {
        return status == MatchStatus.PENDING && !isExpired();
    }
}

enum MatchStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    COMPLETED,
    CANCELLED
}
