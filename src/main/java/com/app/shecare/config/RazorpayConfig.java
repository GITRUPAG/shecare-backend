package com.app.shecare.config;

import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    @Value("${razorpay.webhook_secret}")
    private String webhookSecret;


    public RazorpayClient getClient() throws Exception {
        return new RazorpayClient(keyId, keySecret);
    }

    public String getKeySecret() {
        return keySecret;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}