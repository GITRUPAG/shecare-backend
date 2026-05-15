package com.app.shecare.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    private static final String API_KEY = "YOUR_FAST2SMS_API_KEY";

    public void sendOtp(String phone, String otp) {

        String url = "https://www.fast2sms.com/dev/bulkV2";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("route", "q");
        body.put("message", "Your SheCare OTP is " + otp);
        body.put("numbers", phone);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(url, request, String.class);
            System.out.println("✅ OTP sent to " + phone);
        } catch (Exception e) {
            System.out.println("❌ Failed to send OTP: " + e.getMessage());
        }
    }
}