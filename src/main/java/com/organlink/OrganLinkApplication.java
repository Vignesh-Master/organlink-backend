package com.organlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * OrganLink Application - Main Entry Point
 * 
 * A blockchain-based decentralized platform for multi-organ donation and transplantation
 * that enhances efficiency, transparency, and security in donor-recipient matching.
 * 
 * Features:
 * - Hospital Portal for medical staff
 * - Organization Portal for policy management
 * - Admin Portal for system management
 * - AI-powered donor-patient matching
 * - Blockchain integration for transparency
 * - IPFS for secure document storage
 * - OCR for signature verification
 * 
 * @author OrganLink Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class OrganLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganLinkApplication.class, args);
        System.out.println("🏥 OrganLink Backend Started Successfully!");
        System.out.println("🔗 Blockchain-powered organ donation platform is ready");
        System.out.println("📊 Server running on: http://localhost:8081");
        System.out.println("📚 API Documentation: http://localhost:8081/swagger-ui.html");
    }
}
