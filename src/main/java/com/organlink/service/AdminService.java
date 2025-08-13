package com.organlink.service;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.*;
import com.organlink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin service for system management operations
 */
@Service
@Transactional
public class AdminService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get system statistics for admin dashboard
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Hospital statistics
        stats.put("totalHospitals", hospitalRepository.count());
        stats.put("activeHospitals", hospitalRepository.countByStatus(HospitalStatus.ACTIVE));
        
        // Organization statistics
        stats.put("totalOrganizations", organizationRepository.count());
        stats.put("activeOrganizations", organizationRepository.countByStatus(OrganizationStatus.ACTIVE));
        
        // Donor statistics
        stats.put("totalDonors", donorRepository.count());
        stats.put("activeDonors", donorRepository.countByStatus(DonorStatus.ACTIVE));
        
        // Patient statistics
        stats.put("totalPatients", patientRepository.count());
        stats.put("waitingPatients", patientRepository.countByStatus(PatientStatus.WAITING));
        
        // Policy statistics
        stats.put("activePolicies", policyRepository.countByStatus(PolicyStatus.IMPLEMENTED));
        stats.put("pendingPolicies", policyRepository.countByStatus(PolicyStatus.PENDING));
        
        // Additional metrics
        stats.put("criticalPatients", patientRepository.countByUrgencyLevel(UrgencyLevel.CRITICAL));
        stats.put("emergencyPatients", patientRepository.countByUrgencyLevel(UrgencyLevel.EMERGENCY));
        
        return stats;
    }

    /**
     * Get all hospitals with pagination
     */
    public Page<Hospital> getHospitals(Pageable pageable) {
        return hospitalRepository.findAll(pageable);
    }

    /**
     * Get hospital by ID
     */
    public Optional<Hospital> getHospitalById(Long id) {
        return hospitalRepository.findById(id);
    }

    /**
     * Create new hospital
     */
    public Hospital createHospital(Hospital hospital) {
        System.out.println("🔧 AdminService: Creating hospital...");

        // Generate unique hospital ID
        String hospitalId = generateHospitalId();
        hospital.setHospitalId(hospitalId);
        System.out.println("Generated Hospital ID: " + hospitalId);

        // Set default status
        hospital.setStatus(HospitalStatus.ACTIVE);
        hospital.setVerificationStatus(VerificationStatus.PENDING);

        System.out.println("Saving hospital to database...");

        // Save hospital
        Hospital savedHospital = hospitalRepository.save(hospital);

        System.out.println("Hospital saved with database ID: " + savedHospital.getId());

        // Create default hospital user account (non-blocking)
        createHospitalUser(savedHospital);

        System.out.println("✅ Hospital creation completed successfully");

        return savedHospital;
    }

    /**
     * Update hospital
     */
    public Hospital updateHospital(Long id, Hospital hospitalDetails) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findById(id);
        if (hospitalOpt.isEmpty()) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }

        Hospital hospital = hospitalOpt.get();
        
        // Update fields
        hospital.setHospitalName(hospitalDetails.getHospitalName());
        hospital.setCountry(hospitalDetails.getCountry());
        hospital.setState(hospitalDetails.getState());
        hospital.setCity(hospitalDetails.getCity());
        hospital.setAddress(hospitalDetails.getAddress());
        hospital.setZipCode(hospitalDetails.getZipCode());
        hospital.setContactPerson(hospitalDetails.getContactPerson());
        hospital.setEmail(hospitalDetails.getEmail());
        hospital.setPhone(hospitalDetails.getPhone());
        hospital.setAlternatePhone(hospitalDetails.getAlternatePhone());
        hospital.setLicenseNumber(hospitalDetails.getLicenseNumber());
        hospital.setAccreditation(hospitalDetails.getAccreditation());
        hospital.setSpecializations(hospitalDetails.getSpecializations());
        hospital.setCapacity(hospitalDetails.getCapacity());
        hospital.setStatus(hospitalDetails.getStatus());
        
        return hospitalRepository.save(hospital);
    }

    /**
     * Delete hospital
     */
    public void deleteHospital(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }
        hospitalRepository.deleteById(id);
    }

    /**
     * Get hospital by hospital ID (for viewing details)
     */
    public Optional<Hospital> getHospitalByHospitalId(String hospitalId) {
        return hospitalRepository.findByHospitalId(hospitalId);
    }

    /**
     * Update hospital status
     */
    public Hospital updateHospitalStatus(Long id, HospitalStatus status) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findById(id);
        if (hospitalOpt.isEmpty()) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }

        Hospital hospital = hospitalOpt.get();
        hospital.setStatus(status);
        return hospitalRepository.save(hospital);
    }

    /**
     * Search hospitals by name or ID
     */
    public Page<Hospital> searchHospitals(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return hospitalRepository.findAll(pageable);
        }
        return hospitalRepository.findByHospitalNameContainingIgnoreCaseOrHospitalIdContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
    }

    /**
     * Get all organizations with pagination
     */
    public Page<Organization> getOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable);
    }

    /**
     * Get organization by ID
     */
    public Optional<Organization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    /**
     * Create new organization
     */
    public Organization createOrganization(Organization organization) {
        // Generate unique organization ID
        organization.setOrganizationId(generateOrganizationId());
        
        // Set default status
        organization.setStatus(OrganizationStatus.ACTIVE);
        organization.setVerificationStatus(VerificationStatus.PENDING);
        organization.setVotingPower(1); // Default voting power
        
        // Save organization
        Organization savedOrganization = organizationRepository.save(organization);
        
        // Create default organization user account
        createOrganizationUser(savedOrganization);
        
        return savedOrganization;
    }

    /**
     * Update organization
     */
    public Organization updateOrganization(Long id, Organization organizationDetails) {
        Optional<Organization> orgOpt = organizationRepository.findById(id);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found with id: " + id);
        }

        Organization organization = orgOpt.get();
        
        // Update fields
        organization.setOrganizationName(organizationDetails.getOrganizationName());
        organization.setOrganizationType(organizationDetails.getOrganizationType());
        organization.setCountry(organizationDetails.getCountry());
        organization.setState(organizationDetails.getState());
        organization.setCity(organizationDetails.getCity());
        organization.setAddress(organizationDetails.getAddress());
        organization.setZipCode(organizationDetails.getZipCode());
        organization.setContactPerson(organizationDetails.getContactPerson());
        organization.setEmail(organizationDetails.getEmail());
        organization.setPhone(organizationDetails.getPhone());
        organization.setAlternatePhone(organizationDetails.getAlternatePhone());
        organization.setWebsite(organizationDetails.getWebsite());
        organization.setRegistrationNumber(organizationDetails.getRegistrationNumber());
        organization.setTaxId(organizationDetails.getTaxId());
        organization.setFocusAreas(organizationDetails.getFocusAreas());
        organization.setDescription(organizationDetails.getDescription());
        organization.setStatus(organizationDetails.getStatus());
        organization.setVotingPower(organizationDetails.getVotingPower());
        
        return organizationRepository.save(organization);
    }

    /**
     * Delete organization
     */
    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("Organization not found with id: " + id);
        }
        organizationRepository.deleteById(id);
    }

    /**
     * Get organization by organization ID (for viewing details)
     */
    public Optional<Organization> getOrganizationByOrganizationId(String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId);
    }

    /**
     * Update organization status
     */
    public Organization updateOrganizationStatus(Long id, OrganizationStatus status) {
        Optional<Organization> orgOpt = organizationRepository.findById(id);
        if (orgOpt.isEmpty()) {
            throw new RuntimeException("Organization not found with id: " + id);
        }

        Organization organization = orgOpt.get();
        organization.setStatus(status);
        return organizationRepository.save(organization);
    }

    /**
     * Search organizations by name or ID
     */
    public Page<Organization> searchOrganizations(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return organizationRepository.findAll(pageable);
        }
        return organizationRepository.findByOrganizationNameContainingIgnoreCaseOrOrganizationIdContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
    }

    // Helper methods
    private String generateHospitalId() {
        String prefix = "HOSP";
        long count = hospitalRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    private String generateOrganizationId() {
        String prefix = "ORG";
        long count = organizationRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    private void createHospitalUser(Hospital hospital) {
        // Check if user already exists with this email or username
        String username = hospital.getHospitalId().toLowerCase();
        String email = hospital.getEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("⚠️ User with username " + username + " already exists, skipping user creation");
            return;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            System.out.println("⚠️ User with email " + email + " already exists, skipping user creation");
            return;
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            // Use hospital ID as default password for easier login
            String defaultPassword = hospital.getHospitalId().toLowerCase();
            user.setPassword(passwordEncoder.encode(defaultPassword));
            System.out.println("🔑 Default password set to: " + defaultPassword);
            user.setRole(UserRole.HOSPITAL);
            user.setTenantId(hospital.getHospitalId());
            userRepository.save(user);
            System.out.println("✅ Hospital user account created successfully");
        } catch (Exception e) {
            System.out.println("❌ Failed to create hospital user: " + e.getMessage());
            // Don't throw exception - hospital creation should succeed even if user creation fails
        }
    }

    private void createOrganizationUser(Organization organization) {
        User user = new User();
        user.setUsername(organization.getOrganizationId().toLowerCase());
        user.setEmail(organization.getEmail());
        user.setPassword(passwordEncoder.encode("org123")); // Default password
        user.setRole(UserRole.ORGANIZATION);
        userRepository.save(user);
    }
}
