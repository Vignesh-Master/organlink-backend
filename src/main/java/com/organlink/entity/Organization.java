package com.organlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Organization entity matching the frontend CreateOrganization form
 */
@Entity
@Table(name = "organizations")
@EntityListeners(AuditingEntityListener.class)
public class Organization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "organization_id", unique = true, nullable = false)
    private String organizationId;
    
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "organization_name", nullable = false)
    private String organizationName;
    
    @NotBlank
    @Column(name = "organization_type", nullable = false)
    private String organizationType;
    
    @NotBlank
    @Column(nullable = false)
    private String country;
    
    @Column
    private String state;
    
    @Column
    private String city;
    
    @Column
    private String address;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    @NotBlank
    @Column(name = "contact_person", nullable = false)
    private String contactPerson;
    
    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Column(nullable = false)
    private String phone;
    
    @Column(name = "alternate_phone")
    private String alternatePhone;
    
    @Column
    private String website;
    
    @Column(name = "registration_number", unique = true)
    private String registrationNumber;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @ElementCollection
    @CollectionTable(name = "organization_focus_areas", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "focus_area")
    private List<String> focusAreas = new ArrayList<>();
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;
    
    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "voting_power")
    private Integer votingPower = 1;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "proposedByOrganization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Policy> proposedPolicies = new ArrayList<>();
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> votes = new ArrayList<>();
    
    // Constructors
    public Organization() {}
    
    public Organization(String organizationId, String organizationName, String organizationType, String country) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationType = organizationType;
        this.country = country;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    
    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    public List<String> getFocusAreas() { return focusAreas; }
    public void setFocusAreas(List<String> focusAreas) { this.focusAreas = focusAreas; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public OrganizationStatus getStatus() { return status; }
    public void setStatus(OrganizationStatus status) { this.status = status; }
    
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    
    public Integer getVotingPower() { return votingPower; }
    public void setVotingPower(Integer votingPower) { this.votingPower = votingPower; }
    
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Policy> getProposedPolicies() { return proposedPolicies; }
    public void setProposedPolicies(List<Policy> proposedPolicies) { this.proposedPolicies = proposedPolicies; }
    
    public List<Vote> getVotes() { return votes; }
    public void setVotes(List<Vote> votes) { this.votes = votes; }
}

enum OrganizationStatus {
    ACTIVE, INACTIVE, SUSPENDED, UNDER_REVIEW
}
