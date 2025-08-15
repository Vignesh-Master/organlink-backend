package com.organlink.service;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.*;
import com.organlink.repository.*;
import com.organlink.blockchain.OrganLinkRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Admin service for system management operations
 */
@Service
@Transactional
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

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

    @Autowired
    private OrganLinkRegistryService blockchainService;

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
        logger.info("🔧 AdminService: Creating hospital...");

        // Generate unique hospital ID
        String hospitalId = generateHospitalId();
        hospital.setHospitalId(hospitalId);
        logger.info("Generated Hospital ID: {}", hospitalId);

        // Generate unique license number if not provided
        if (hospital.getLicenseNumber() == null || hospital.getLicenseNumber().isEmpty()) {
            String licenseNumber = generateLicenseNumber();
            hospital.setLicenseNumber(licenseNumber);
            logger.info("Generated License Number: {}", licenseNumber);
        }

        // Generate blockchain address for hospital
        String blockchainAddress = generateBlockchainAddress();
        hospital.setBlockchainAddress(blockchainAddress);
        logger.info("Generated Blockchain Address: {}", blockchainAddress);

        // Set default status
        hospital.setStatus(HospitalStatus.ACTIVE);
        hospital.setVerificationStatus(VerificationStatus.PENDING);

        logger.info("Saving hospital to database...");

        // Save hospital
        Hospital savedHospital = hospitalRepository.save(hospital);

        logger.info("Hospital saved with database ID: {}", savedHospital.getId());

        // Authorize hospital on blockchain (asynchronously)
        authorizeHospitalOnBlockchain(savedHospital);

        // Create default hospital user account (non-blocking)
        createHospitalUser(savedHospital);

        logger.info("✅ Hospital creation completed successfully");

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

    private String generateLicenseNumber() {
        String prefix = "LIC";
        long count = hospitalRepository.count() + 1;
        // Add some randomness to avoid duplicates
        int random = (int) (Math.random() * 1000);
        return prefix + String.format("%03d", count) + String.format("%03d", random);
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
            // Use password from hospital creation form, fallback to hospital ID
            String password = hospital.getPassword() != null && !hospital.getPassword().isEmpty()
                ? hospital.getPassword()
                : hospital.getHospitalId().toLowerCase();
            user.setPassword(passwordEncoder.encode(password));
            System.out.println("🔑 Password set for hospital login");
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

    /**
     * Generate a mock blockchain address for hospital
     * In production, this would be generated by the hospital's wallet
     */
    private String generateBlockchainAddress() {
        // For now, we'll generate a mock Ethereum address
        // In production, hospitals would provide their actual wallet addresses
        SecureRandom random = new SecureRandom();
        byte[] addressBytes = new byte[20];
        random.nextBytes(addressBytes);
        
        StringBuilder addressBuilder = new StringBuilder("0x");
        for (byte b : addressBytes) {
            addressBuilder.append(String.format("%02x", b));
        }
        return addressBuilder.toString();
    }

    /**
     * Authorize hospital on blockchain asynchronously
     */
    private void authorizeHospitalOnBlockchain(Hospital hospital) {
        CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🔗 Authorizing hospital {} on blockchain...", hospital.getHospitalId());
                
                // Call blockchain service to authorize hospital
                String transactionHash = blockchainService.authorizeHospital(
                    hospital.getBlockchainAddress(), 
                    hospital.getHospitalId()
                ).get(); // Wait for transaction to complete
                
                // Update hospital with authorization transaction hash
                hospital.setAuthorizationTxHash(transactionHash);
                hospitalRepository.save(hospital);
                
                logger.info("✅ Hospital authorized on blockchain. Tx Hash: {}", transactionHash);
                return transactionHash;
                
            } catch (Exception e) {
                logger.error("❌ Failed to authorize hospital on blockchain: {}", e.getMessage(), e);
                throw new RuntimeException("Blockchain authorization failed", e);
            }
        }).exceptionally(throwable -> {
            logger.error("❌ Blockchain authorization failed for hospital {}: {}", 
                hospital.getHospitalId(), throwable.getMessage());
            return null;
        });
    }
}
