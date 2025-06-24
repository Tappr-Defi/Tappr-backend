package com.semicolon.africa.tapprbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DatabaseConfig {
    // This class can be used for database-specific configurations
    // Currently, we're handling DDL issues through application properties
}