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
 * Patient entity matching the frontend RegisterPatient form
 */
@Entity
@Table(name = "patients")
@EntityListeners(AuditingEntityListener.class)
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "patient_id", unique = true, nullable = false)
    private String patientId;
    
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
    @NotBlank
    @Column(name = "organ_needed", nullable = false)
    private String organNeeded;
    
    @Column(name = "medical_condition", columnDefinition = "TEXT")
    private String medicalCondition;
    
    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;
    
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
    
    // Urgency and Priority
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false)
    private UrgencyLevel urgencyLevel;
    
    @Column(name = "priority_score")
    private Double priorityScore;
    
    @Column(name = "waiting_list_date")
    private LocalDate waitingListDate;
    
    @Column(name = "estimated_survival_time")
    private Integer estimatedSurvivalTime; // in days
    
    // Insurance and Financial
    @Column(name = "insurance_provider")
    private String insuranceProvider;
    
    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;
    
    @Column(name = "financial_status")
    private String financialStatus;
    
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
    private PatientStatus status = PatientStatus.REGISTERED;
    
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
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matches = new ArrayList<>();
    
    // Constructors
    public Patient() {}
    
    public Patient(String patientId, String firstName, String lastName, LocalDate dateOfBirth, String bloodType, String organNeeded) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.organNeeded = organNeeded;
    }
    
    // Getters and Setters (similar pattern as Donor)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
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
    
    public String getOrganNeeded() { return organNeeded; }
    public void setOrganNeeded(String organNeeded) { this.organNeeded = organNeeded; }
    
    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }
    
    public LocalDate getDiagnosisDate() { return diagnosisDate; }
    public void setDiagnosisDate(LocalDate diagnosisDate) { this.diagnosisDate = diagnosisDate; }
    
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
    
    public UrgencyLevel getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(UrgencyLevel urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public Double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Double priorityScore) { this.priorityScore = priorityScore; }
    
    public LocalDate getWaitingListDate() { return waitingListDate; }
    public void setWaitingListDate(LocalDate waitingListDate) { this.waitingListDate = waitingListDate; }
    
    public Integer getEstimatedSurvivalTime() { return estimatedSurvivalTime; }
    public void setEstimatedSurvivalTime(Integer estimatedSurvivalTime) { this.estimatedSurvivalTime = estimatedSurvivalTime; }
    
    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }
    
    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }
    
    public String getFinancialStatus() { return financialStatus; }
    public void setFinancialStatus(String financialStatus) { this.financialStatus = financialStatus; }
    
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
    
    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }
    
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
    
    public int getWaitingTime() {
        if (waitingListDate != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(waitingListDate, LocalDate.now());
        }
        return 0;
    }
}

enum UrgencyLevel {
    LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY
}

enum PatientStatus {
    REGISTERED, ACTIVE, WAITING, MATCHED, TRANSPLANTED, DECEASED, SUSPENDED, REMOVED
}
