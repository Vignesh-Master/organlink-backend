package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes legacy location endpoint used by the current frontend at hospital login.
 * This avoids authentication issues by keeping the endpoint public and at the exact path
 * /api/v1/hospital/cities-by-state expected by the UI.
 */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class LegacyLocationController {

    @Autowired
    private LocationService locationService;

    /**
     * Legacy compatibility route used by the frontend:
     * GET /api/v1/hospital/cities-by-state?stateId=...
     */
    @GetMapping("/api/v1/hospital/cities-by-state")
    public ResponseEntity<ApiResponse<List<String>>> getCitiesLegacy(@RequestParam("stateId") String stateId) {
        List<String> cities = locationService.getCitiesByState(stateId);
        List<String> deduped = cities.stream().distinct().toList();
        return ResponseEntity.ok(ApiResponse.success("Cities retrieved", deduped));
    }
}
