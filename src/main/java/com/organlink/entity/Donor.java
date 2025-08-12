package com.organlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Donor entity matching the frontend RegisterDonor form
 */
@Entity
@Table(name = "donors")
@EntityListeners(AuditingEntityListener.class)
public class Donor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "donor_id", unique = true, nullable = false)
    private String donorId;
    
    // Personal Information
    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @NotBlank
    @Column(name = "blood_type", nullable = false)
    private String bloodType;
    
    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Column(nullable = false)
    private String phone;
    
    @Column(name = "alternate_phone")
    private String alternatePhone;
    
    // Address Information
    @NotBlank
    @Column(nullable = false)
    private String address;
    
    @NotBlank
    @Column(nullable = false)
    private String city;
    
    @NotBlank
    @Column(nullable = false)
    private String state;
    
    @NotBlank
    @Column(nullable = false)
    private String country;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    // Emergency Contact
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;
    
    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;
    
    // Medical Information
    @ElementCollection
    @CollectionTable(name = "donor_organ_types", joinColumns = @JoinColumn(name = "donor_id"))
    @Column(name = "organ_type")
    private List<String> organTypes = new ArrayList<>();
    
    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;
    
    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;
    
    @Column(name = "allergies")
    private String allergies;
    
    @Column(name = "height")
    private Double height; // in cm
    
    @Column(name = "weight")
    private Double weight; // in kg
    
    @Column(name = "bmi")
    private Double bmi;
    
    // Lifestyle Information
    @Column(name = "smoking_status")
    private String smokingStatus;
    
    @Column(name = "alcohol_consumption")
    private String alcoholConsumption;
    
    @Column(name = "exercise_frequency")
    private String exerciseFrequency;
    
    // Consent and Legal
    @Column(name = "consent_given", nullable = false)
    private Boolean consentGiven = false;
    
    @Column(name = "signature_file_path")
    private String signatureFilePath;
    
    @Column(name = "signature_ipfs_hash")
    private String signatureIpfsHash;
    
    @Column(name = "signature_verified")
    private Boolean signatureVerified = false;
    
    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;
    
    // Status and Tracking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonorStatus status = DonorStatus.REGISTERED;
    
    @Column(name = "availability_status")
    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;
    
    @Column(name = "last_medical_checkup")
    private LocalDate lastMedicalCheckup;
    
    @Column(name = "next_checkup_due")
    private LocalDate nextCheckupDue;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
    
    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matches = new ArrayList<>();
    
    // Constructors
    public Donor() {}
    
    public Donor(String donorId, String firstName, String lastName, LocalDate dateOfBirth, String bloodType) {
        this.donorId = donorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    
    public String getEmergencyContactRelationship() { return emergencyContactRelationship; }
    public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }
    
    public List<String> getOrganTypes() { return organTypes; }
    public void setOrganTypes(List<String> organTypes) { this.organTypes = organTypes; }
    
    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    
    public String getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(String currentMedications) { this.currentMedications = currentMedications; }
    
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
    
    public String getSmokingStatus() { return smokingStatus; }
    public void setSmokingStatus(String smokingStatus) { this.smokingStatus = smokingStatus; }
    
    public String getAlcoholConsumption() { return alcoholConsumption; }
    public void setAlcoholConsumption(String alcoholConsumption) { this.alcoholConsumption = alcoholConsumption; }
    
    public String getExerciseFrequency() { return exerciseFrequency; }
    public void setExerciseFrequency(String exerciseFrequency) { this.exerciseFrequency = exerciseFrequency; }
    
    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }
    
    public String getSignatureFilePath() { return signatureFilePath; }
    public void setSignatureFilePath(String signatureFilePath) { this.signatureFilePath = signatureFilePath; }
    
    public String getSignatureIpfsHash() { return signatureIpfsHash; }
    public void setSignatureIpfsHash(String signatureIpfsHash) { this.signatureIpfsHash = signatureIpfsHash; }
    
    public Boolean getSignatureVerified() { return signatureVerified; }
    public void setSignatureVerified(Boolean signatureVerified) { this.signatureVerified = signatureVerified; }
    
    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }
    
    public DonorStatus getStatus() { return status; }
    public void setStatus(DonorStatus status) { this.status = status; }
    
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    
    public LocalDate getLastMedicalCheckup() { return lastMedicalCheckup; }
    public void setLastMedicalCheckup(LocalDate lastMedicalCheckup) { this.lastMedicalCheckup = lastMedicalCheckup; }
    
    public LocalDate getNextCheckupDue() { return nextCheckupDue; }
    public void setNextCheckupDue(LocalDate nextCheckupDue) { this.nextCheckupDue = nextCheckupDue; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    
    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }
    
    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    public boolean isAvailable() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE && status == DonorStatus.ACTIVE;
    }
}

enum Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

enum DonorStatus {
    REGISTERED, ACTIVE, INACTIVE, MATCHED, DONATED, DECEASED, SUSPENDED
}

enum AvailabilityStatus {
    AVAILABLE, TEMPORARILY_UNAVAILABLE, MATCHED, NOT_AVAILABLE
}
