package com.app.shecare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shecare.dto.AuthResponse;
import com.app.shecare.dto.LoginRequest;
import com.app.shecare.entity.User;
import com.app.shecare.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
        try {
            String result = userService.register(request);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.login(
                    request.getIdentifier(),
                    request.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // ✅ 401 with readable message instead of 403
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage() != null
                            ? e.getMessage()
                            : "Invalid credentials"));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {

        System.out.println("🔐 Google login attempt received");

        String firebaseToken = body.get("token");

        if (firebaseToken == null || firebaseToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Firebase token is required"));
        }

        System.out.println("Token received, length: " + firebaseToken.length());
        System.out.println("Firebase apps initialized: " + com.google.firebase.FirebaseApp.getApps().size());

        try {
            System.out.println("Verifying Firebase token...");
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);

            String email    = decoded.getEmail();
            String name     = decoded.getName();
            String googleId = decoded.getUid();
            String picture  = decoded.getPicture();

            System.out.println("✅ Token verified for email: " + email);

            AuthResponse response = userService.googleLogin(email, name, googleId, picture);
            System.out.println("✅ Google login successful for: " + email);

            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            System.out.println("❌ FirebaseAuthException: " + e.getMessage());
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid or expired Google token"));

        } catch (Exception e) {
            System.out.println("❌ Unexpected exception: " + e.getClass().getName());
            System.out.println("❌ Message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage() != null
                            ? e.getMessage() : "Internal server error"));
        }
    }
}