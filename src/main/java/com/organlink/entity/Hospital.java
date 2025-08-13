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
 * Hospital entity matching the frontend CreateHospital form
 */
@Entity
@Table(name = "hospitals")
@EntityListeners(AuditingEntityListener.class)
public class Hospital {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "hospital_id", unique = true, nullable = false)
    private String hospitalId;
    
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;
    
    @NotBlank
    @Column(nullable = false)
    private String country;
    
    @NotBlank
    @Column(nullable = false)
    private String state;
    
    @NotBlank
    @Column(nullable = false)
    private String city;
    
    @NotBlank
    @Column(nullable = false)
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
    
    @NotBlank
    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;
    
    @Column(name = "accreditation")
    private String accreditation;
    
    @ElementCollection
    @CollectionTable(name = "hospital_specializations", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "specialization")
    private List<String> specializations = new ArrayList<>();
    
    @Column(name = "capacity")
    private Integer capacity;

    @Transient // Don't store password in database, only use for user creation
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HospitalStatus status = HospitalStatus.ACTIVE;
    
    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donor> donors = new ArrayList<>();
    
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients = new ArrayList<>();
    
    // Constructors
    public Hospital() {}
    
    public Hospital(String hospitalId, String hospitalName, String country, String state, String city) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.country = country;
        this.state = state;
        this.city = city;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    
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
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public String getAccreditation() { return accreditation; }
    public void setAccreditation(String accreditation) { this.accreditation = accreditation; }
    
    public List<String> getSpecializations() { return specializations; }
    public void setSpecializations(List<String> specializations) { this.specializations = specializations; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public HospitalStatus getStatus() { return status; }
    public void setStatus(HospitalStatus status) { this.status = status; }
    
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Donor> getDonors() { return donors; }
    public void setDonors(List<Donor> donors) { this.donors = donors; }
    
    public List<Patient> getPatients() { return patients; }
    public void setPatients(List<Patient> patients) { this.patients = patients; }

    // Password management for admin operations
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

// Enums moved to separate files
