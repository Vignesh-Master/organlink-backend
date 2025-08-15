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
