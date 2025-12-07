package com.tappr.finance.tapprbackend.kyc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "smile-id")
public class SmileIdConfig {
    private String partnerId;
    private String apiKey;
    private String url;
    private String callbackUrl;
}