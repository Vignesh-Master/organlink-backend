package com.organlink.service;

import com.organlink.dto.LoginRequest;
import com.organlink.dto.LoginResponse;
import com.organlink.entity.Hospital;
import com.organlink.entity.Organization;
import com.organlink.entity.User;
import com.organlink.entity.UserRole;
import com.organlink.repository.HospitalRepository;
import com.organlink.repository.OrganizationRepository;
import com.organlink.repository.UserRepository;
import com.organlink.security.CustomUserDetailsService;
import com.organlink.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Authentication service for handling login, token validation, and user management
 */
@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authenticate admin user
     */
    public LoginResponse authenticateAdmin(LoginRequest.AdminLoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) userDetails;

            // Verify user role
            if (!userPrincipal.getRole().equals("ADMIN")) {
                throw new BadCredentialsException("Invalid admin credentials");
            }

            // Update last login
            User user = userPrincipal.getUser();
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Generate tokens
            String token = jwtUtil.generateToken(userDetails, "ADMIN", null);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Create user info
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                "ADMIN",
                "System Administrator"
            );

            return new LoginResponse(token, refreshToken, userInfo, null);

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid admin credentials");
        }
    }

    /**
     * Authenticate hospital user
     */
    public LoginResponse authenticateHospital(LoginRequest.HospitalLoginRequest request) {
        try {
            System.out.println("🏥 Hospital login attempt:");
            System.out.println("Hospital ID: " + request.getHospitalId());
            System.out.println("User ID: " + request.getUserId());
            System.out.println("Password: " + request.getPassword());

            // Find hospital
            Optional<Hospital> hospitalOpt = hospitalRepository.findByHospitalId(request.getHospitalId());
            if (hospitalOpt.isEmpty()) {
                System.out.println("❌ Hospital not found: " + request.getHospitalId());
                throw new BadCredentialsException("Hospital not found");
            }

            Hospital hospital = hospitalOpt.get();
            System.out.println("✅ Hospital found: " + hospital.getHospitalName());

            // Authenticate user with tenant context
            System.out.println("🔍 Looking for user: " + request.getUserId() + " with tenant: " + request.getHospitalId());
            UserDetails userDetails = userDetailsService.loadUserByUsernameAndTenant(
                request.getUserId(), request.getHospitalId()
            );
            System.out.println("✅ User found: " + userDetails.getUsername());

            // Verify password
            System.out.println("🔐 Verifying password...");
            System.out.println("Provided password: " + request.getPassword());
            System.out.println("Stored password hash: " + userDetails.getPassword());

            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                System.out.println("❌ Password verification failed");
                throw new BadCredentialsException("Invalid credentials");
            }
            System.out.println("✅ Password verified successfully");

            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) userDetails;

            // Verify user role
            if (!userPrincipal.getRole().equals("HOSPITAL")) {
                throw new BadCredentialsException("Invalid hospital user credentials");
            }

            // Update last login
            User user = userPrincipal.getUser();
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Update hospital last activity
            hospital.setLastActivity(LocalDateTime.now());
            hospitalRepository.save(hospital);

            // Generate tokens
            String token = jwtUtil.generateToken(userDetails, "HOSPITAL", request.getHospitalId());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Create user info
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                "HOSPITAL",
                "Hospital User"
            );
            userInfo.setHospitalId(request.getHospitalId());

            return new LoginResponse(token, refreshToken, userInfo, request.getHospitalId());

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid hospital credentials");
        }
    }

    /**
     * Authenticate organization user
     */
    public LoginResponse authenticateOrganization(LoginRequest.OrganizationLoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) userDetails;

            // Verify user role
            if (!userPrincipal.getRole().equals("ORGANIZATION")) {
                throw new BadCredentialsException("Invalid organization credentials");
            }

            // Update last login
            User user = userPrincipal.getUser();
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Find associated organization (assuming username matches organizationId or there's a mapping)
            Optional<Organization> orgOpt = organizationRepository.findByEmail(user.getEmail());
            String organizationId = orgOpt.map(Organization::getOrganizationId).orElse(null);

            if (orgOpt.isPresent()) {
                Organization org = orgOpt.get();
                org.setLastActivity(LocalDateTime.now());
                organizationRepository.save(org);
            }

            // Generate tokens
            String token = jwtUtil.generateToken(userDetails, "ORGANIZATION", organizationId);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Create user info
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                "ORGANIZATION",
                "Organization Representative"
            );
            userInfo.setOrganizationId(organizationId);

            return new LoginResponse(token, refreshToken, userInfo, organizationId);

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid organization credentials");
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        return jwtUtil.validateToken(token);
    }

    /**
     * Get current user information from token
     */
    public LoginResponse.UserInfo getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        String tenantId = jwtUtil.extractTenantId(token);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("User not found");
        }

        User user = userOpt.get();
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            role,
            getUserDisplayName(user, role)
        );

        if ("HOSPITAL".equals(role)) {
            userInfo.setHospitalId(tenantId);
        } else if ("ORGANIZATION".equals(role)) {
            userInfo.setOrganizationId(tenantId);
        }

        return userInfo;
    }

    /**
     * Refresh JWT token
     */
    public LoginResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String newToken = jwtUtil.refreshToken(token);
        
        LoginResponse.UserInfo userInfo = getCurrentUser(authHeader);
        return new LoginResponse(newToken, null, userInfo, jwtUtil.extractTenantId(token));
    }

    /**
     * Logout user (token blacklisting would be implemented here)
     */
    public void logout(String authHeader) {
        // In a production system, you would add the token to a blacklist
        // For now, we'll just validate that the token exists
        if (!validateToken(authHeader)) {
            throw new BadCredentialsException("Invalid token");
        }
        // Token blacklisting logic would go here
    }

    private String getUserDisplayName(User user, String role) {
        switch (role) {
            case "ADMIN":
                return "System Administrator";
            case "HOSPITAL":
                return "Hospital User";
            case "ORGANIZATION":
                return "Organization Representative";
            default:
                return "User";
        }
    }
}
