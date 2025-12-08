package com.tappr.finance.tapprbackend.user.services.implementations;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
    @Async
    public void emitWebhook(String event, String userId, String details) {
        System.out.println("Webhook: " + event + ", User: " + userId + ", Details: " + details);
    }
}