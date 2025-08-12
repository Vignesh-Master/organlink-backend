package com.organlink.dto;

/**
 * Login response DTO
 * Contains authentication token and user information
 */
public class LoginResponse {
    
    private String token;
    private String refreshToken;
    private UserInfo user;
    private String tenantId;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }
    
    public LoginResponse(String token, String refreshToken, UserInfo user, String tenantId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    /**
     * User information DTO
     */
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String role;
        private String name;
        private String hospitalId;
        private String organizationId;
        
        // Constructors
        public UserInfo() {}
        
        public UserInfo(String id, String username, String email, String role, String name) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.name = name;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getHospitalId() { return hospitalId; }
        public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
        
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    }
}
