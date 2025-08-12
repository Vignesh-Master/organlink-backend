package com.organlink.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTOs for different user types
 */
public class LoginRequest {
    
    /**
     * Admin login request
     */
    public static class AdminLoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Password is required")
        private String password;
        
        // Constructors
        public AdminLoginRequest() {}
        
        public AdminLoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    /**
     * Hospital login request
     */
    public static class HospitalLoginRequest {
        @NotBlank(message = "Country is required")
        private String countryId;
        
        @NotBlank(message = "State is required")
        private String stateId;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "Hospital is required")
        private String hospitalId;
        
        @NotBlank(message = "User ID is required")
        private String userId;
        
        @NotBlank(message = "Password is required")
        private String password;
        
        // Constructors
        public HospitalLoginRequest() {}
        
        // Getters and Setters
        public String getCountryId() { return countryId; }
        public void setCountryId(String countryId) { this.countryId = countryId; }
        
        public String getStateId() { return stateId; }
        public void setStateId(String stateId) { this.stateId = stateId; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getHospitalId() { return hospitalId; }
        public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    /**
     * Organization login request
     */
    public static class OrganizationLoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Password is required")
        private String password;
        
        // Constructors
        public OrganizationLoginRequest() {}
        
        public OrganizationLoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
