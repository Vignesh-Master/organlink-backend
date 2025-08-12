package com.organlink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration for OrganLink
 * Enables JPA auditing, repositories, and transaction management
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.organlink.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA configuration is handled by application.yml
    // This class enables additional JPA features
}
