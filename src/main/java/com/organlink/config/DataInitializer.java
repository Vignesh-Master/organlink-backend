package com.organlink.config;

import com.organlink.entity.User;
import com.organlink.entity.UserRole;
import com.organlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${organlink.admin.default-username}")
    private String adminUsername;

    @Value("${organlink.admin.default-password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if it doesn't exist
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setEmail("admin@organlink.com"); // Default email
            adminUser.setRole(UserRole.ADMIN);
            // The 'enabled' property is part of Spring Security's UserDetails, not our User entity.
            userRepository.save(adminUser);
            System.out.println("✅ Default admin user created successfully!");
        }
    }
}
