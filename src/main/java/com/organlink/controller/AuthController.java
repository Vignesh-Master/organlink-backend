package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.dto.LoginRequest;
import com.organlink.dto.LoginResponse;
import com.organlink.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for all user types
 * Handles login requests for Admin, Hospital, and Organization users
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Admin login endpoint
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(
            @Valid @RequestBody LoginRequest.AdminLoginRequest request) {
        try {
            LoginResponse response = authService.authenticateAdmin(request);
            return ResponseEntity.ok(ApiResponse.success("Admin login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Admin login failed", e.getMessage()));
        }
    }

    /**
     * Hospital login endpoint
     */
    @PostMapping("/hospital/login")
    public ResponseEntity<ApiResponse<LoginResponse>> hospitalLogin(
            @Valid @RequestBody LoginRequest.HospitalLoginRequest request) {
        try {
            LoginResponse response = authService.authenticateHospital(request);
            return ResponseEntity.ok(ApiResponse.success("Hospital login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Hospital login failed", e.getMessage()));
        }
    }

    /**
     * Organization login endpoint
     */
    @PostMapping("/organization/login")
    public ResponseEntity<ApiResponse<LoginResponse>> organizationLogin(
            @Valid @RequestBody LoginRequest.OrganizationLoginRequest request) {
        try {
            LoginResponse response = authService.authenticateOrganization(request);
            return ResponseEntity.ok(ApiResponse.success("Organization login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Organization login failed", e.getMessage()));
        }
    }

    /**
     * Token validation endpoint
     */
    @GetMapping("/auth/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            boolean isValid = authService.validateToken(authHeader);
            return ResponseEntity.ok(ApiResponse.success("Token validation result", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token validation failed", e.getMessage()));
        }
    }

    /**
     * Get current user information
     */
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        try {
            LoginResponse.UserInfo userInfo = authService.getCurrentUser(authHeader);
            return ResponseEntity.ok(ApiResponse.success("User information retrieved", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get user information", e.getMessage()));
        }
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            LoginResponse response = authService.refreshToken(authHeader);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token refresh failed", e.getMessage()));
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        try {
            authService.logout(authHeader);
            return ResponseEntity.ok(ApiResponse.success("Logout successful", "User logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Logout failed", e.getMessage()));
        }
    }
}
