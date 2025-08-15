package com.organlink.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Intercepts requests and validates JWT tokens
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

        // Skip JWT processing for public endpoints
        String requestPath = request.getRequestURI();
        if (isPublicEndpoint(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token");
            } catch (Exception e) {
                logger.error("JWT Token has expired or is invalid");
            }
        }

        // Once we get the token validate it
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails;
                
                // Extract tenant information from JWT for hospital users
                String tenantId = jwtUtil.extractTenantId(jwtToken);
                String role = jwtUtil.extractRole(jwtToken);
                
                // Use tenant-aware loading for hospital users
                if ("HOSPITAL".equals(role) && tenantId != null) {
                    CustomUserDetailsService customUserDetailsService = (CustomUserDetailsService) this.userDetailsService;
                    userDetails = customUserDetailsService.loadUserByUsernameAndTenant(username, tenantId);
                } else {
                    userDetails = this.userDetailsService.loadUserByUsername(username);
                }

                // If token is valid configure Spring Security to manually set authentication
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (Exception e) {
                logger.error("Error during JWT authentication: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Check if the endpoint is public and should skip JWT processing
     */
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.equals("/") ||
               requestPath.startsWith("/api/v1/health") ||
               requestPath.startsWith("/api/v1/info") ||
               requestPath.startsWith("/swagger-ui") ||
               requestPath.startsWith("/v3/api-docs") ||
               requestPath.startsWith("/swagger-resources") ||
               requestPath.equals("/api/v1/admin/login") ||
               requestPath.equals("/api/v1/hospital/login") ||
               requestPath.equals("/api/v1/organization/login") ||
               requestPath.startsWith("/api/v1/locations/") ||
               requestPath.startsWith("/api/v1/hospital/cities-by-state") ||
               requestPath.startsWith("/api/v1/hospital/hospitals-by-city");
    }
}
