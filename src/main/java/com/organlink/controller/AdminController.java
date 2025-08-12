package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.Hospital;
import com.organlink.entity.Organization;
import com.organlink.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Admin controller for system management
 * Handles hospital and organization management, system statistics
 */
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Get system statistics for admin dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        try {
            Map<String, Object> stats = adminService.getSystemStats();
            return ResponseEntity.ok(ApiResponse.success("System statistics retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve system statistics", e.getMessage()));
        }
    }

    // Hospital Management Endpoints

    /**
     * Get all hospitals with pagination
     */
    @GetMapping("/hospitals")
    public ResponseEntity<ApiResponse<Page<Hospital>>> getHospitals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Hospital> hospitals = adminService.getHospitals(pageable);
            return ResponseEntity.ok(ApiResponse.success("Hospitals retrieved", hospitals));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve hospitals", e.getMessage()));
        }
    }

    /**
     * Get hospital by ID
     */
    @GetMapping("/hospitals/{id}")
    public ResponseEntity<ApiResponse<Hospital>> getHospitalById(@PathVariable Long id) {
        try {
            Optional<Hospital> hospital = adminService.getHospitalById(id);
            if (hospital.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Hospital retrieved", hospital.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve hospital", e.getMessage()));
        }
    }

    /**
     * Create new hospital
     */
    @PostMapping("/hospitals")
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@Valid @RequestBody Hospital hospital) {
        try {
            Hospital createdHospital = adminService.createHospital(hospital);
            return ResponseEntity.ok(ApiResponse.success("Hospital created successfully", createdHospital));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create hospital", e.getMessage()));
        }
    }

    /**
     * Update hospital
     */
    @PutMapping("/hospitals/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(
            @PathVariable Long id, 
            @Valid @RequestBody Hospital hospital) {
        try {
            Hospital updatedHospital = adminService.updateHospital(id, hospital);
            return ResponseEntity.ok(ApiResponse.success("Hospital updated successfully", updatedHospital));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update hospital", e.getMessage()));
        }
    }

    /**
     * Delete hospital
     */
    @DeleteMapping("/hospitals/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHospital(@PathVariable Long id) {
        try {
            adminService.deleteHospital(id);
            return ResponseEntity.ok(ApiResponse.success("Hospital deleted successfully", "Hospital removed from system"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete hospital", e.getMessage()));
        }
    }

    // Organization Management Endpoints

    /**
     * Get all organizations with pagination
     */
    @GetMapping("/organizations")
    public ResponseEntity<ApiResponse<Page<Organization>>> getOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Organization> organizations = adminService.getOrganizations(pageable);
            return ResponseEntity.ok(ApiResponse.success("Organizations retrieved", organizations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve organizations", e.getMessage()));
        }
    }

    /**
     * Get organization by ID
     */
    @GetMapping("/organizations/{id}")
    public ResponseEntity<ApiResponse<Organization>> getOrganizationById(@PathVariable Long id) {
        try {
            Optional<Organization> organization = adminService.getOrganizationById(id);
            if (organization.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Organization retrieved", organization.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve organization", e.getMessage()));
        }
    }

    /**
     * Create new organization
     */
    @PostMapping("/organizations")
    public ResponseEntity<ApiResponse<Organization>> createOrganization(@Valid @RequestBody Organization organization) {
        try {
            Organization createdOrganization = adminService.createOrganization(organization);
            return ResponseEntity.ok(ApiResponse.success("Organization created successfully", createdOrganization));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create organization", e.getMessage()));
        }
    }

    /**
     * Update organization
     */
    @PutMapping("/organizations/{id}")
    public ResponseEntity<ApiResponse<Organization>> updateOrganization(
            @PathVariable Long id, 
            @Valid @RequestBody Organization organization) {
        try {
            Organization updatedOrganization = adminService.updateOrganization(id, organization);
            return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", updatedOrganization));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update organization", e.getMessage()));
        }
    }

    /**
     * Delete organization
     */
    @DeleteMapping("/organizations/{id}")
    public ResponseEntity<ApiResponse<String>> deleteOrganization(@PathVariable Long id) {
        try {
            adminService.deleteOrganization(id);
            return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", "Organization removed from system"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete organization", e.getMessage()));
        }
    }
}
