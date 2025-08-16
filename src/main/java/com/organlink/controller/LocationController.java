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
 * Provides normalized country/state/city/hospital location data for dropdowns.
 * Ensures deduplicated states per country and cities per state, so the frontend
 * does not show duplicated Tamil Nadu etc. even when multiple cities exist.
 */
@RestController
@RequestMapping("/api/v1/locations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * List countries present in DB (or default fallback)
     */
    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCountries() {
        List<Map<String, String>> countries = locationService.getCountries();
        return ResponseEntity.ok(ApiResponse.success("Countries retrieved", countries));
    }

    /**
     * List states for a given country. countryId is an uppercase/underscore ID like INDIA or UNITED_STATES
     */
    @GetMapping("/states")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getStates(@RequestParam("countryId") String countryId) {
        List<Map<String, String>> states = locationService.getStatesByCountry(countryId);
        // Deduplicate by id in case DB returns duplicates
        List<Map<String, String>> deduped = states.stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(m -> m.get("id"), m -> m, (a, b) -> a, java.util.LinkedHashMap::new),
                        m -> new java.util.ArrayList<>(m.values())));
        return ResponseEntity.ok(ApiResponse.success("States retrieved", deduped));
    }

    /**
     * List cities for a given stateId (e.g., TAMIL_NADU, KA, etc.)
     */
    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getCities(@RequestParam("stateId") String stateId) {
        List<String> cities = locationService.getCitiesByState(stateId);
        // Deduplicate
        List<String> deduped = cities.stream().distinct().toList();
        return ResponseEntity.ok(ApiResponse.success("Cities retrieved", deduped));
    }

    /**
     * Legacy compatibility route used by the current frontend:
     * GET /api/v1/hospital/cities-by-state?stateId=...
     * Make this public and delegate to the same service to avoid 401 on login.
     */
    @GetMapping(path = "/api/v1/hospital/cities-by-state")
    public ResponseEntity<ApiResponse<List<String>>> getCitiesLegacy(@RequestParam("stateId") String stateId) {
        List<String> cities = locationService.getCitiesByState(stateId);
        List<String> deduped = cities.stream().distinct().toList();
        return ResponseEntity.ok(ApiResponse.success("Cities retrieved", deduped));
    }

    /**
     * List hospitals for a given city and state
     */
    @GetMapping("/hospitals")
    public ResponseEntity<ApiResponse<List<Hospital>>> getHospitals(
            @RequestParam("city") String city,
            @RequestParam("stateId") String stateId) {
        List<Hospital> hospitals = locationService.getHospitalsByCity(city, stateId);
        return ResponseEntity.ok(ApiResponse.success("Hospitals retrieved", hospitals));
    }
}
