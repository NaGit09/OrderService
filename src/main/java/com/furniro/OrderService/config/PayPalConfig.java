package com.furniro.OrderService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String secret;

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }
}