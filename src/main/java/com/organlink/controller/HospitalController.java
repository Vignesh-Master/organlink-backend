package com.organlink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organlink.dto.ApiResponse;
import com.organlink.dto.DonorRegistrationRequest;
import com.organlink.entity.Donor;
import com.organlink.entity.Gender;
import com.organlink.entity.Patient;
import com.organlink.security.CustomUserDetailsService;
import com.organlink.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

/**
 * Hospital controller for donor and patient management
 * Handles hospital-specific operations with tenant isolation
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
@PreAuthorize("hasRole('HOSPITAL')")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * Get hospital dashboard statistics
     */
    @GetMapping("/hospital/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Map<String, Object> stats = hospitalService.getDashboardStats(hospitalId);
            return ResponseEntity.ok(ApiResponse.success("Hospital dashboard stats retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve dashboard stats", e.getMessage()));
        }
    }

    // Donor Management Endpoints

    /**
     * Alternative JSON-based donor registration to match frontend (file uploaded via /signatures first)
     */
    @PostMapping(value = "/donors", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Donor>> registerDonorJson(
            @RequestBody Map<String, Object> donorPayload,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);

            // Map minimal fields from payload to Donor entity
            Donor donor = new Donor();
            donor.setDonorId((String) donorPayload.getOrDefault("donorId", ""));
            donor.setFirstName((String) donorPayload.getOrDefault("firstName", ""));
            donor.setLastName((String) donorPayload.getOrDefault("lastName", ""));
            // dateOfBirth expected as ISO string
            if (donorPayload.get("dateOfBirth") != null) {
                donor.setDateOfBirth(java.time.LocalDate.parse((String) donorPayload.get("dateOfBirth")));
            }
            if (donorPayload.get("gender") != null) {
                donor.setGender(com.organlink.entity.Gender.valueOf(((String) donorPayload.get("gender")).toUpperCase()));
            }
            // Frontend sends bloodGroup
            donor.setBloodType((String) donorPayload.getOrDefault("bloodGroup", ""));
            donor.setEmail((String) donorPayload.getOrDefault("email", ""));
            donor.setPhone((String) donorPayload.getOrDefault("phone", ""));
            donor.setAlternatePhone((String) donorPayload.getOrDefault("alternatePhone", null));
            donor.setAddress((String) donorPayload.getOrDefault("address", ""));
            donor.setCity((String) donorPayload.getOrDefault("city", ""));
            donor.setState((String) donorPayload.getOrDefault("state", (String) donorPayload.getOrDefault("stateId", "")));
            donor.setCountry((String) donorPayload.getOrDefault("country", "India"));
            donor.setZipCode((String) donorPayload.getOrDefault("zipCode", null));
            donor.setEmergencyContactName((String) donorPayload.getOrDefault("emergencyContactName", null));
            donor.setEmergencyContactPhone((String) donorPayload.getOrDefault("emergencyContactPhone", null));
            donor.setEmergencyContactRelationship((String) donorPayload.getOrDefault("emergencyContactRelationship", null));
            if (donorPayload.get("height") != null) donor.setHeight(Double.valueOf(donorPayload.get("height").toString()));
            if (donorPayload.get("weight") != null) donor.setWeight(Double.valueOf(donorPayload.get("weight").toString()));

            // Signature data returned from /signatures/verify-and-store
            @SuppressWarnings("unchecked")
            Map<String, Object> signatureData = (Map<String, Object>) donorPayload.get("signatureData");
            if (signatureData == null || signatureData.get("ipfsHash") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Signature data missing", "Please upload and verify signature first."));
            }
            donor.setSignatureIpfsHash((String) signatureData.get("ipfsHash"));
            // If OCR said verified, mark
            boolean verified = false;
            if (signatureData.get("ocrResult") instanceof Map<?, ?> ocr) {
                Object v = ((Map<?, ?>) ocr).get("verified");
                verified = v != null && Boolean.parseBoolean(v.toString());
            }
            donor.setSignatureVerified(verified);

            // Reuse service path by bypassing OCR/IPFS (already done); save and chain blockchain registration
            // We'll mimic HospitalService.registerDonor internals without redoing file upload
            // Fetch hospital
            String hId = hospitalId;
            java.util.Optional<com.organlink.entity.Hospital> hospitalOpt = hospitalService.getHospitalRepository().findByHospitalId(hId);
            if (hospitalOpt.isEmpty()) {
                throw new RuntimeException("Hospital not found: " + hId);
            }
            com.organlink.entity.Hospital hospital = hospitalOpt.get();

            donor.setDonorId(donor.getDonorId() == null || donor.getDonorId().isEmpty() ?
                    hId + "-DON-" + String.format("%04d", hospitalService.getDonorRepository().count() + 1) : donor.getDonorId());
            donor.setHospital(hospital);
            donor.setStatus(com.organlink.entity.DonorStatus.REGISTERED);
            donor.setAvailabilityStatus(com.organlink.entity.AvailabilityStatus.AVAILABLE);

            if (donor.getHeight() != null && donor.getWeight() != null) {
                double m = donor.getHeight() / 100.0;
                donor.setBmi(donor.getWeight() / (m * m));
            }

            Donor saved = hospitalService.getDonorRepository().save(donor);

            // Create signature record
            hospitalService.createSignatureRecordPublic(donor.getSignatureIpfsHash(), "DONOR_CONSENT", saved.getId(), "DONOR", hospital);

            // Register on blockchain asynchronously
            hospitalService.registerDonorOnBlockchainPublic(saved, donor.getSignatureIpfsHash());

            return ResponseEntity.ok(ApiResponse.success("Donor registered successfully", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to register donor (JSON)", e.getMessage()));
        }
    }

    /**
     * Alternative JSON-based patient registration to match frontend (file uploaded via /signatures first)
     */
    @PostMapping(value = "/patients", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Patient>> registerPatientJson(
            @RequestBody Map<String, Object> patientPayload,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);

            Patient patient = new Patient();
            patient.setPatientId((String) patientPayload.getOrDefault("patientId", ""));
            patient.setFirstName((String) patientPayload.getOrDefault("firstName", ""));
            patient.setLastName((String) patientPayload.getOrDefault("lastName", ""));
            if (patientPayload.get("dateOfBirth") != null) {
                patient.setDateOfBirth(java.time.LocalDate.parse((String) patientPayload.get("dateOfBirth")));
            }
            if (patientPayload.get("gender") != null) {
                patient.setGender(com.organlink.entity.Gender.valueOf(((String) patientPayload.get("gender")).toUpperCase()));
            }
            // Frontend uses bloodGroup
            patient.setBloodType((String) patientPayload.getOrDefault("bloodGroup", ""));
            // Organ needed may come as organTypeId string; fallback to organTypeName or direct text
            String organNeeded = (String) patientPayload.getOrDefault("organNeeded", null);
            if (organNeeded == null) organNeeded = (String) patientPayload.getOrDefault("organTypeName", null);
            if (organNeeded == null) organNeeded = (String) patientPayload.getOrDefault("organ", "");
            patient.setOrganNeeded(organNeeded);
            patient.setEmail((String) patientPayload.getOrDefault("email", ""));
            patient.setPhone((String) patientPayload.getOrDefault("phone", ""));
            patient.setAlternatePhone((String) patientPayload.getOrDefault("alternatePhone", null));
            patient.setAddress((String) patientPayload.getOrDefault("address", ""));
            patient.setCity((String) patientPayload.getOrDefault("city", ""));
            patient.setState((String) patientPayload.getOrDefault("state", (String) patientPayload.getOrDefault("stateId", "")));
            patient.setCountry((String) patientPayload.getOrDefault("country", "India"));
            patient.setZipCode((String) patientPayload.getOrDefault("zipCode", null));
            if (patientPayload.get("height") != null) patient.setHeight(Double.valueOf(patientPayload.get("height").toString()));
            if (patientPayload.get("weight") != null) patient.setWeight(Double.valueOf(patientPayload.get("weight").toString()));
            // Urgency
            String urgency = (String) patientPayload.getOrDefault("urgencyLevel", "MEDIUM");
            patient.setUrgencyLevel(com.organlink.entity.UrgencyLevel.valueOf(urgency.toUpperCase()));

            @SuppressWarnings("unchecked")
            Map<String, Object> signatureData = (Map<String, Object>) patientPayload.get("signatureData");
            if (signatureData == null || signatureData.get("ipfsHash") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Signature data missing", "Please upload and verify signature first."));
            }
            patient.setSignatureIpfsHash((String) signatureData.get("ipfsHash"));
            boolean verified = false;
            if (signatureData.get("ocrResult") instanceof Map<?, ?> ocr) {
                Object v = ((Map<?, ?>) ocr).get("verified");
                verified = v != null && Boolean.parseBoolean(v.toString());
            }
            patient.setSignatureVerified(verified);

            // Fetch hospital
            java.util.Optional<com.organlink.entity.Hospital> hospitalOpt = hospitalService.getHospitalRepository().findByHospitalId(hospitalId);
            if (hospitalOpt.isEmpty()) {
                throw new RuntimeException("Hospital not found: " + hospitalId);
            }
            com.organlink.entity.Hospital hospital = hospitalOpt.get();

            patient.setPatientId(patient.getPatientId() == null || patient.getPatientId().isEmpty() ?
                    hospitalId + "-PAT-" + String.format("%04d", hospitalService.getPatientRepository().count() + 1) : patient.getPatientId());
            patient.setHospital(hospital);
            patient.setStatus(com.organlink.entity.PatientStatus.REGISTERED);
            patient.setWaitingListDate(java.time.LocalDate.now());

            if (patient.getHeight() != null && patient.getWeight() != null) {
                double m = patient.getHeight() / 100.0;
                patient.setBmi(patient.getWeight() / (m * m));
            }
            patient.setPriorityScore(hospitalService.calculatePriorityScorePublic(patient));

            Patient saved = hospitalService.getPatientRepository().save(patient);
            hospitalService.createSignatureRecordPublic(patient.getSignatureIpfsHash(), "PATIENT_CONSENT", saved.getId(), "PATIENT", hospital);
            hospitalService.registerPatientOnBlockchainPublic(saved, patient.getSignatureIpfsHash());

            return ResponseEntity.ok(ApiResponse.success("Patient registered successfully", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to register patient (JSON)", e.getMessage()));
        }
    }

    /**
     * Register new donor with signature upload (matches frontend format)
     */
    @PostMapping(value = "/donors", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Donor>> registerDonor(
            @RequestParam("donorData") String donorDataJson,
            @RequestPart("signatureFile") MultipartFile signatureFile,
            @RequestParam("signerName") String signerName,
            @RequestParam("signerType") String signerType,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            
            // Parse the JSON donor data
            ObjectMapper objectMapper = new ObjectMapper();
            DonorRegistrationRequest donorRequest = objectMapper.readValue(donorDataJson, DonorRegistrationRequest.class);
            
            // Convert DTO to Entity
            Donor donor = convertDonorRequestToEntity(donorRequest);
            
            Donor registeredDonor = hospitalService.registerDonor(donor, hospitalId, signatureFile, signerName);
            
            return ResponseEntity.ok(ApiResponse.success("Donor registered successfully", registeredDonor));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to register donor", e.getMessage()));
        }
    }

    /**
     * Get donors for hospital with pagination
     */
    @GetMapping("/donors")
    public ResponseEntity<ApiResponse<Page<Donor>>> getDonors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Pageable pageable = PageRequest.of(page, size);
            Page<Donor> donors = hospitalService.getDonors(hospitalId, pageable);
            return ResponseEntity.ok(ApiResponse.success("Donors retrieved", donors));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve donors", e.getMessage()));
        }
    }

    /**
     * Get donor by ID
     */
    @GetMapping("/donors/{id}")
    public ResponseEntity<ApiResponse<Donor>> getDonorById(
            @PathVariable Long id, 
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Optional<Donor> donor = hospitalService.getDonorById(id, hospitalId);
            if (donor.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Donor retrieved", donor.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve donor", e.getMessage()));
        }
    }

    /**
     * Update donor
     */
    @PutMapping("/donors/{id}")
    public ResponseEntity<ApiResponse<Donor>> updateDonor(
            @PathVariable Long id,
            @Valid @RequestBody Donor donor,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Donor updatedDonor = hospitalService.updateDonor(id, donor, hospitalId);
            return ResponseEntity.ok(ApiResponse.success("Donor updated successfully", updatedDonor));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update donor", e.getMessage()));
        }
    }

    // Patient Management Endpoints

    /**
     * Register new patient with signature upload
     */
    @PostMapping(value = "/patients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Patient>> registerPatient(
            @RequestPart("patient") @Valid Patient patient,
            @RequestPart("signatureImage") MultipartFile signatureImage,
            @RequestParam("signatureName") String signatureName,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Patient registeredPatient = hospitalService.registerPatient(patient, hospitalId, signatureImage, signatureName);
            
            // Blockchain recording is now handled within HospitalService
            
            return ResponseEntity.ok(ApiResponse.success("Patient registered successfully", registeredPatient));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to register patient", e.getMessage()));
        }
    }

    /**
     * Get patients for hospital with pagination
     */
    @GetMapping("/patients")
    public ResponseEntity<ApiResponse<Page<Patient>>> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Pageable pageable = PageRequest.of(page, size);
            Page<Patient> patients = hospitalService.getPatients(hospitalId, pageable);
            return ResponseEntity.ok(ApiResponse.success("Patients retrieved", patients));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve patients", e.getMessage()));
        }
    }

    /**
     * Get patient by ID
     */
    @GetMapping("/patients/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatientById(
            @PathVariable Long id, 
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Optional<Patient> patient = hospitalService.getPatientById(id, hospitalId);
            if (patient.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Patient retrieved", patient.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve patient", e.getMessage()));
        }
    }

    /**
     * Update patient
     */
    @PutMapping("/patients/{id}")
    public ResponseEntity<ApiResponse<Patient>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody Patient patient,
            Authentication authentication) {
        try {
            String hospitalId = getHospitalIdFromAuth(authentication);
            Patient updatedPatient = hospitalService.updatePatient(id, patient, hospitalId);
            return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", updatedPatient));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update patient", e.getMessage()));
        }
    }

    // Helper method to convert DonorRegistrationRequest to Donor entity
    private Donor convertDonorRequestToEntity(DonorRegistrationRequest request) {
        Donor donor = new Donor();
        
        donor.setDonorId(request.getDonorId());
        donor.setFirstName(request.getFirstName());
        donor.setLastName(request.getLastName());
        donor.setDateOfBirth(request.getDateOfBirth());
        
        // Convert gender string to enum
        if (request.getGender() != null) {
            donor.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        }
        
        donor.setBloodType(request.getBloodGroup());
        donor.setEmail(request.getEmail());
        donor.setPhone(request.getPhone());
        donor.setAlternatePhone(request.getAlternatePhone());
        donor.setAddress(request.getAddress());
        donor.setCity(request.getCity());
        donor.setState(request.getState());
        donor.setCountry(request.getCountry());
        donor.setZipCode(request.getZipCode());
        donor.setEmergencyContactName(request.getEmergencyContactName());
        donor.setEmergencyContactPhone(request.getEmergencyContactPhone());
        donor.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        donor.setOrganTypes(request.getOrganTypes());
        donor.setMedicalHistory(request.getMedicalHistory());
        donor.setCurrentMedications(request.getCurrentMedications());
        donor.setAllergies(request.getAllergies());
        donor.setHeight(request.getHeight());
        donor.setWeight(request.getWeight());
        donor.setSmokingStatus(request.getSmokingStatus());
        donor.setAlcoholConsumption(request.getAlcoholConsumption());
        donor.setExerciseFrequency(request.getExerciseFrequency());
        donor.setConsentGiven(true); // Set to true since they're registering
        
        return donor;
    }
    
    // Helper method to extract hospital ID from authentication
    private String getHospitalIdFromAuth(Authentication authentication) {
        CustomUserDetailsService.CustomUserPrincipal principal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        return principal.getTenantId();
    }
}
