package com.app.shecare.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static class OtpData {
        String otp;
        Instant expiry;
        int attempts;

        OtpData(String otp, Instant expiry) {
            this.otp = otp;
            this.expiry = expiry;
            this.attempts = 0;
        }
    }

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final int EXPIRY_SECONDS = 300; // 5 minutes

    public String generateOtp(String phone) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        otpStore.put(phone, new OtpData(
                otp,
                Instant.now().plusSeconds(EXPIRY_SECONDS)
        ));

        return otp;
    }

    public boolean verifyOtp(String phone, String inputOtp) {

        OtpData data = otpStore.get(phone);

        if (data == null) return false;

        // Expired
        if (Instant.now().isAfter(data.expiry)) {
            otpStore.remove(phone);
            return false;
        }

        // Too many attempts
        if (data.attempts >= MAX_ATTEMPTS) {
            otpStore.remove(phone);
            return false;
        }

        // Check OTP
        if (!data.otp.equals(inputOtp)) {
            data.attempts++;
            return false;
        }

        // Success → remove OTP
        otpStore.remove(phone);
        return true;
    }
}