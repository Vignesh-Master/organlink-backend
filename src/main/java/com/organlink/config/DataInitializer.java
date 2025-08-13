package com.organlink.config;

import com.organlink.entity.User;
import com.organlink.entity.UserRole;
import com.organlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initializer to create default admin account
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminAccount();
    }

    /**
     * Create default admin account: admin/admin123
     */
    private void createDefaultAdminAccount() {
        // Check if admin already exists
        if (userRepository.findByUsername("admin").isPresent()) {
            System.out.println("✅ Default admin account already exists");
            return;
        }

        // Create default admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@organlink.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        userRepository.save(admin);
        
        System.out.println("✅ Default admin account created:");
        System.out.println("   Username: admin");
        System.out.println("   Password: admin123");
        System.out.println("   Login URL: http://localhost:8080/admin/login");
    }
}
