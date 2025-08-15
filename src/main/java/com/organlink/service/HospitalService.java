package com.organlink.service;

import com.organlink.entity.*;
import com.organlink.repository.*;
import com.organlink.blockchain.OrganLinkRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hospital service for donor and patient management
 */
@Service
@Transactional
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SignatureRecordRepository signatureRecordRepository;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private IPFSService ipfsService;

    @Autowired
    private OrganLinkRegistryService blockchainService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);


    /**
     * Get hospital dashboard statistics
     */
    public Map<String, Object> getDashboardStats(String hospitalId) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findByHospitalId(hospitalId);
        if (hospitalOpt.isEmpty()) {
            throw new RuntimeException("Hospital not found: " + hospitalId);
        }

        Hospital hospital = hospitalOpt.get();
        Map<String, Object> stats = new HashMap<>();
        
        // Donor statistics
        stats.put("totalDonors", donorRepository.countByHospitalId(hospital.getId()));
        stats.put("activeDonors", donorRepository.findByHospitalId(hospital.getId()).stream()
                .filter(donor -> donor.getStatus() == DonorStatus.ACTIVE)
                .count());
        
        // Patient statistics
        stats.put("totalPatients", patientRepository.countByHospitalId(hospital.getId()));
        stats.put("waitingPatients", patientRepository.findByHospitalId(hospital.getId()).stream()
                .filter(patient -> patient.getStatus() == PatientStatus.WAITING)
                .count());
        
        // Match statistics
        stats.put("activeMatches", matchRepository.countByHospitalId(hospital.getId()));
        stats.put("successfulTransplants", matchRepository.countCompletedByHospitalId(hospital.getId()));
        
        // Recent activity
        // Get recent registrations (simplified)
        List<Donor> recentDonors = donorRepository.findByHospitalId(hospital.getId()).stream()
                .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
        List<Patient> recentPatients = patientRepository.findByHospitalId(hospital.getId()).stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        stats.put("recentDonorRegistrations", recentDonors);
        stats.put("recentPatientRegistrations", recentPatients);
        
        // Blockchain verification statistics
        long verifiedDonors = donorRepository.findByHospitalId(hospital.getId()).stream()
                .filter(donor -> donor.getSignatureVerified() != null && donor.getSignatureVerified())
                .count();
        long verifiedPatients = patientRepository.findByHospitalId(hospital.getId()).stream()
                .filter(patient -> patient.getSignatureVerified() != null && patient.getSignatureVerified())
                .count();
        
        stats.put("verifiedDonors", verifiedDonors);
        stats.put("verifiedPatients", verifiedPatients);
        
        // IPFS and blockchain integration stats
        long ipfsRecords = signatureRecordRepository.countByUploadedBy(hospital);
        stats.put("ipfsRecords", ipfsRecords);
        
        // Recent matches for this hospital
        List<Match> recentMatches = matchRepository.findMatchesForHospital(hospitalId).stream()
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
        stats.put("recentMatches", recentMatches);
        
        // Cross-hospital matches (where donor and patient are from different hospitals)
        long crossHospitalMatches = matchRepository.findMatchesForHospital(hospitalId).stream()
                .filter(match -> !match.getPatient().getHospital().getId().equals(match.getDonor().getHospital().getId()))
                .count();
        stats.put("crossHospitalMatches", crossHospitalMatches);
        
        // Notification statistics
        Optional<User> hospitalUser = userRepository.findByTenantId(hospitalId);
        if (hospitalUser.isPresent()) {
            List<Notification> unreadNotifications = notificationService.getUnreadNotificationsForUser(hospitalUser.get().getId());
            stats.put("unreadNotifications", unreadNotifications.size());
            stats.put("recentNotifications", unreadNotifications.stream().limit(5).collect(java.util.stream.Collectors.toList()));
        } else {
            stats.put("unreadNotifications", 0);
            stats.put("recentNotifications", java.util.Collections.emptyList());
        }
        
        return stats;
    }

    /**
     * Register new donor with signature verification
     */
    public Donor registerDonor(Donor donor, String hospitalId, MultipartFile signatureImage, String signatureName) throws Exception {
        // 1. Verify Signature
        if (!ocrService.verifySignature(signatureImage, signatureName)) {
            throw new Exception("Signature verification failed. The name on the signature does not match the provided name.");
        }

        // 2. Upload to IPFS
        String ipfsHash = ipfsService.uploadFile(signatureImage);
        donor.setSignatureIpfsHash(ipfsHash); // Corrected method name
        donor.setSignatureVerified(true);

        // 3. Save Donor and get ID
        Optional<Hospital> hospitalOpt = hospitalRepository.findByHospitalId(hospitalId);
        if (hospitalOpt.isEmpty()) {
            throw new RuntimeException("Hospital not found: " + hospitalId);
        }
        Hospital hospital = hospitalOpt.get();
        
        donor.setDonorId(generateDonorId(hospitalId));
        donor.setHospital(hospital);
        donor.setStatus(DonorStatus.REGISTERED);
        donor.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        
        if (donor.getHeight() != null && donor.getWeight() != null) {
            double heightInMeters = donor.getHeight() / 100.0;
            double bmi = donor.getWeight() / (heightInMeters * heightInMeters);
            donor.setBmi(bmi);
        }
        
        Donor savedDonor = donorRepository.save(donor);

        // 4. Create Signature Record
        createSignatureRecord(ipfsHash, "DONOR_CONSENT", savedDonor.getId(), "DONOR", hospital);

        // 5. Register on blockchain
        registerDonorOnBlockchain(savedDonor, ipfsHash);

        return savedDonor;
    }

    /**
     * Register new patient with signature verification
     */
    public Patient registerPatient(Patient patient, String hospitalId, MultipartFile signatureImage, String signatureName) throws Exception {
        // 1. Verify Signature
        if (!ocrService.verifySignature(signatureImage, signatureName)) {
            throw new Exception("Signature verification failed. The name on the signature does not match the provided name.");
        }

        // 2. Upload to IPFS
        String ipfsHash = ipfsService.uploadFile(signatureImage);
        patient.setSignatureIpfsHash(ipfsHash); // Corrected method name
        patient.setSignatureVerified(true);

        // 3. Save Patient and get ID
        Optional<Hospital> hospitalOpt = hospitalRepository.findByHospitalId(hospitalId);
        if (hospitalOpt.isEmpty()) {
            throw new RuntimeException("Hospital not found: " + hospitalId);
        }
        Hospital hospital = hospitalOpt.get();

        patient.setPatientId(generatePatientId(hospitalId));
        patient.setHospital(hospital);
        patient.setStatus(PatientStatus.REGISTERED);
        patient.setWaitingListDate(LocalDate.now());

        if (patient.getHeight() != null && patient.getWeight() != null) {
            double heightInMeters = patient.getHeight() / 100.0;
            double bmi = patient.getWeight() / (heightInMeters * heightInMeters);
            patient.setBmi(bmi);
        }

        patient.setPriorityScore(calculatePriorityScore(patient));
        
        Patient savedPatient = patientRepository.save(patient);

        // 4. Create Signature Record
        createSignatureRecord(ipfsHash, "PATIENT_CONSENT", savedPatient.getId(), "PATIENT", hospital);

        // 5. Register on blockchain
        registerPatientOnBlockchain(savedPatient, ipfsHash);

        return savedPatient;
    }


    /**
     * Get donors for hospital with pagination
     */
    public Page<Donor> getDonors(String hospitalId, Pageable pageable) {
        return donorRepository.findByHospitalHospitalId(hospitalId, pageable);
    }

    /**
     * Get donor by ID
     */
    public Optional<Donor> getDonorById(Long id, String hospitalId) {
        Optional<Donor> donorOpt = donorRepository.findById(id);
        if (donorOpt.isPresent() && donorOpt.get().getHospital().getHospitalId().equals(hospitalId)) {
            return donorOpt;
        }
        return Optional.empty();
    }

    /**
     * Update donor
     */
    public Donor updateDonor(Long id, Donor donorDetails, String hospitalId) {
        Optional<Donor> donorOpt = getDonorById(id, hospitalId);
        if (donorOpt.isEmpty()) {
            throw new RuntimeException("Donor not found or access denied");
        }

        Donor donor = donorOpt.get();
        
        // Update fields
        donor.setFirstName(donorDetails.getFirstName());
        donor.setLastName(donorDetails.getLastName());
        donor.setDateOfBirth(donorDetails.getDateOfBirth());
        donor.setGender(donorDetails.getGender());
        donor.setBloodType(donorDetails.getBloodType());
        donor.setEmail(donorDetails.getEmail());
        donor.setPhone(donorDetails.getPhone());
        donor.setAlternatePhone(donorDetails.getAlternatePhone());
        donor.setAddress(donorDetails.getAddress());
        donor.setCity(donorDetails.getCity());
        donor.setState(donorDetails.getState());
        donor.setCountry(donorDetails.getCountry());
        donor.setZipCode(donorDetails.getZipCode());
        donor.setEmergencyContactName(donorDetails.getEmergencyContactName());
        donor.setEmergencyContactPhone(donorDetails.getEmergencyContactPhone());
        donor.setEmergencyContactRelationship(donorDetails.getEmergencyContactRelationship());
        donor.setOrganTypes(donorDetails.getOrganTypes());
        donor.setMedicalHistory(donorDetails.getMedicalHistory());
        donor.setCurrentMedications(donorDetails.getCurrentMedications());
        donor.setAllergies(donorDetails.getAllergies());
        donor.setHeight(donorDetails.getHeight());
        donor.setWeight(donorDetails.getWeight());
        donor.setSmokingStatus(donorDetails.getSmokingStatus());
        donor.setAlcoholConsumption(donorDetails.getAlcoholConsumption());
        donor.setExerciseFrequency(donorDetails.getExerciseFrequency());
        donor.setStatus(donorDetails.getStatus());
        donor.setAvailabilityStatus(donorDetails.getAvailabilityStatus());
        
        // Recalculate BMI
        if (donor.getHeight() != null && donor.getWeight() != null) {
            double heightInMeters = donor.getHeight() / 100.0;
            double bmi = donor.getWeight() / (heightInMeters * heightInMeters);
            donor.setBmi(bmi);
        }
        
        return donorRepository.save(donor);
    }

    /**
     * Get patients for hospital with pagination
     */
    public Page<Patient> getPatients(String hospitalId, Pageable pageable) {
        return patientRepository.findByHospitalHospitalId(hospitalId, pageable);
    }

    /**
     * Get patient by ID
     */
    public Optional<Patient> getPatientById(Long id, String hospitalId) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent() && patientOpt.get().getHospital().getHospitalId().equals(hospitalId)) {
            return patientOpt;
        }
        return Optional.empty();
    }

    /**
     * Update patient
     */
    public Patient updatePatient(Long id, Patient patientDetails, String hospitalId) {
        Optional<Patient> patientOpt = getPatientById(id, hospitalId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found or access denied");
        }

        Patient patient = patientOpt.get();
        
        // Update fields
        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setGender(patientDetails.getGender());
        patient.setBloodType(patientDetails.getBloodType());
        patient.setEmail(patientDetails.getEmail());
        patient.setPhone(patientDetails.getPhone());
        patient.setAlternatePhone(patientDetails.getAlternatePhone());
        patient.setAddress(patientDetails.getAddress());
        patient.setCity(patientDetails.getCity());
        patient.setState(patientDetails.getState());
        patient.setCountry(patientDetails.getCountry());
        patient.setZipCode(patientDetails.getZipCode());
        patient.setEmergencyContactName(patientDetails.getEmergencyContactName());
        patient.setEmergencyContactPhone(patientDetails.getEmergencyContactPhone());
        patient.setEmergencyContactRelationship(patientDetails.getEmergencyContactRelationship());
        patient.setOrganNeeded(patientDetails.getOrganNeeded());
        patient.setMedicalCondition(patientDetails.getMedicalCondition());
        patient.setDiagnosisDate(patientDetails.getDiagnosisDate());
        patient.setCurrentMedications(patientDetails.getCurrentMedications());
        patient.setAllergies(patientDetails.getAllergies());
        patient.setHeight(patientDetails.getHeight());
        patient.setWeight(patientDetails.getWeight());
        patient.setUrgencyLevel(patientDetails.getUrgencyLevel());
        patient.setEstimatedSurvivalTime(patientDetails.getEstimatedSurvivalTime());
        patient.setInsuranceProvider(patientDetails.getInsuranceProvider());
        patient.setInsurancePolicyNumber(patientDetails.getInsurancePolicyNumber());
        patient.setFinancialStatus(patientDetails.getFinancialStatus());
        patient.setStatus(patientDetails.getStatus());
        
        // Recalculate BMI
        if (patient.getHeight() != null && patient.getWeight() != null) {
            double heightInMeters = patient.getHeight() / 100.0;
            double bmi = patient.getWeight() / (heightInMeters * heightInMeters);
            patient.setBmi(bmi);
        }
        
        // Recalculate priority score
        patient.setPriorityScore(calculatePriorityScore(patient));
        
        return patientRepository.save(patient);
    }

    // Helper methods
    private String generateDonorId(String hospitalId) {
        long count = donorRepository.count() + 1;
        return hospitalId + "-DON-" + String.format("%04d", count);
    }

    private String generatePatientId(String hospitalId) {
        long count = patientRepository.count() + 1;
        return hospitalId + "-PAT-" + String.format("%04d", count);
    }

    private Double calculatePriorityScore(Patient patient) {
        double score = 0.0;
        
        // Urgency level weight
        if (patient.getUrgencyLevel() != null) {
            switch (patient.getUrgencyLevel()) {
                case EMERGENCY -> score += 100;
                case CRITICAL -> score += 80;
                case HIGH -> score += 60;
                case MEDIUM -> score += 40;
                case LOW -> score += 20;
            }
        }
        
        // Waiting time weight (days waiting)
        if (patient.getWaitingListDate() != null) {
            long daysWaiting = java.time.temporal.ChronoUnit.DAYS.between(
                patient.getWaitingListDate(), LocalDate.now());
            score += daysWaiting * 0.1; // 0.1 points per day
        }
        
        // Age factor (children get priority)
        int age = patient.getAge();
        if (age < 18) {
            score += 20; // Pediatric bonus
        } else if (age > 65) {
            score -= 10; // Senior penalty
        }
        
        return score;
    }
    
    private void createSignatureRecord(String ipfsHash, String docType, Long entityId, String entityType, Hospital hospital) {
        SignatureRecord record = new SignatureRecord();
        record.setIpfsHash(ipfsHash);
        record.setDocumentType(docType);
        record.setEntityId(entityId);
        record.setEntityType(entityType);
        record.setUploadedBy(hospital);
        // The blockchainTxHash will be set later by an async process
        signatureRecordRepository.save(record);
    }

    /**
     * Register donor on blockchain asynchronously
     */
    private void registerDonorOnBlockchain(Donor donor, String ipfsHash) {
        try {
            logger.info("🔗 Registering donor {} on blockchain...", donor.getDonorId());
            
            String fullName = donor.getFirstName() + " " + donor.getLastName();
            
            // Register donor on blockchain
            blockchainService.registerDonor(
                donor.getDonorId(),
                fullName,
                donor.getBloodType().toString(),
                ipfsHash
            ).thenAccept(transactionHash -> {
                logger.info("✅ Donor {} registered on blockchain. Tx Hash: {}", 
                    donor.getDonorId(), transactionHash);
            }).exceptionally(throwable -> {
                logger.error("❌ Failed to register donor {} on blockchain: {}", 
                    donor.getDonorId(), throwable.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            logger.error("❌ Error initiating donor blockchain registration: {}", e.getMessage(), e);
        }
    }

    /**
     * Register patient on blockchain asynchronously
     */
    private void registerPatientOnBlockchain(Patient patient, String ipfsHash) {
        try {
            logger.info("🔗 Registering patient {} on blockchain...", patient.getPatientId());
            
            String fullName = patient.getFirstName() + " " + patient.getLastName();
            
            // Register patient on blockchain
            blockchainService.registerPatient(
                patient.getPatientId(),
                fullName,
                patient.getBloodType().toString(),
                patient.getOrganNeeded(),
                patient.getUrgencyLevel().toString(),
                ipfsHash
            ).thenAccept(transactionHash -> {
                logger.info("✅ Patient {} registered on blockchain. Tx Hash: {}", 
                    patient.getPatientId(), transactionHash);
            }).exceptionally(throwable -> {
                logger.error("❌ Failed to register patient {} on blockchain: {}", 
                    patient.getPatientId(), throwable.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            logger.error("❌ Error initiating patient blockchain registration: {}", e.getMessage(), e);
        }
    }

    /**
     * Verify donor signature on blockchain
     */
    public void verifyDonorSignature(String donorId, boolean verified) {
        try {
            logger.info("🔗 Verifying donor signature for {} as {}", donorId, verified);
            
            blockchainService.verifyDonorSignature(donorId, verified)
                .thenAccept(transactionHash -> {
                    logger.info("✅ Donor signature verification completed. Tx Hash: {}", transactionHash);
                })
                .exceptionally(throwable -> {
                    logger.error("❌ Failed to verify donor signature: {}", throwable.getMessage());
                    return null;
                });
                
        } catch (Exception e) {
            logger.error("❌ Error verifying donor signature: {}", e.getMessage(), e);
        }
    }

    /**
     * Verify patient signature on blockchain
     */
    public void verifyPatientSignature(String patientId, boolean verified) {
        try {
            logger.info("🔗 Verifying patient signature for {} as {}", patientId, verified);
            
            blockchainService.verifyPatientSignature(patientId, verified)
                .thenAccept(transactionHash -> {
                    logger.info("✅ Patient signature verification completed. Tx Hash: {}", transactionHash);
                })
                .exceptionally(throwable -> {
                    logger.error("❌ Failed to verify patient signature: {}", throwable.getMessage());
                    return null;
                });
                
        } catch (Exception e) {
            logger.error("❌ Error verifying patient signature: {}", e.getMessage(), e);
        }
    }
}
