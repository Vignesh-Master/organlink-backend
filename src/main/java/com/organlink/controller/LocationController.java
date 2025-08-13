package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.entity.Hospital;
import com.organlink.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Location controller for country, state, city, and hospital data
 * Provides location hierarchy for frontend forms
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * Get all countries
     */
    @GetMapping("/locations/countries")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCountries() {
        try {
            List<Map<String, String>> countries = locationService.getCountries();
            return ResponseEntity.ok(ApiResponse.success("Countries retrieved", countries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve countries", e.getMessage()));
        }
    }

    /**
     * Get states by country
     */
    @GetMapping("/locations/states")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getStates(
            @RequestParam(required = false) String countryId) {
        try {
            if (countryId == null || countryId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Country ID is required", "countryId parameter is missing"));
            }
            
            List<Map<String, String>> states = locationService.getStatesByCountry(countryId);
            return ResponseEntity.ok(ApiResponse.success("States retrieved", states));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve states", e.getMessage()));
        }
    }

    /**
     * Get cities by state (Hospital-specific endpoint)
     */
    @GetMapping("/hospital/cities-by-state")
    public ResponseEntity<ApiResponse<List<String>>> getCitiesByState(
            @RequestParam String stateId) {
        try {
            if (stateId == null || stateId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("State ID is required", "stateId parameter is missing"));
            }
            
            List<String> cities = locationService.getCitiesByState(stateId);
            return ResponseEntity.ok(ApiResponse.success("Cities retrieved", cities));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve cities", e.getMessage()));
        }
    }

    /**
     * Get hospitals by city and state (Hospital-specific endpoint)
     */
    @GetMapping("/hospital/hospitals-by-city")
    public ResponseEntity<ApiResponse<List<Hospital>>> getHospitalsByCity(
            @RequestParam String city,
            @RequestParam String stateId) {
        try {
            System.out.println("🏥 Hospital lookup request:");
            System.out.println("City: " + city);
            System.out.println("StateId: " + stateId);

            if (city == null || city.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("City is required", "city parameter is missing"));
            }

            if (stateId == null || stateId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("State ID is required", "stateId parameter is missing"));
            }

            List<Hospital> hospitals = locationService.getHospitalsByCity(city, stateId);
            System.out.println("Found " + hospitals.size() + " hospitals");

            return ResponseEntity.ok(ApiResponse.success("Hospitals retrieved", hospitals));
        } catch (Exception e) {
            System.out.println("❌ Hospital lookup failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve hospitals", e.getMessage()));
        }
    }

    /**
     * Get all hospitals (for admin purposes)
     */
    @GetMapping("/locations/hospitals")
    public ResponseEntity<ApiResponse<List<Hospital>>> getAllHospitals() {
        try {
            List<Hospital> hospitals = locationService.getAllHospitals();
            return ResponseEntity.ok(ApiResponse.success("All hospitals retrieved", hospitals));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve hospitals", e.getMessage()));
        }
    }
}
