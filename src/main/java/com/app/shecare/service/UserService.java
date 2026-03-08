package com.app.shecare.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shecare.dto.AuthResponse;
import com.app.shecare.entity.Profile;
import com.app.shecare.entity.Role;
import com.app.shecare.entity.User;
import com.app.shecare.repository.ProfileRepository;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.security.JwtService;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    // ─── REGISTER ────────────────────────────────────────────────────────────
    public String register(User request) {

        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
            (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())) {
            throw new RuntimeException("Email or Phone number is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole(Role.ROLE_USER);
        request.setEnabled(true);
        request.setEmailVerified(false);
        request.setPhoneVerified(false);

        User savedUser = userRepository.save(request);

        Profile profile = Profile.builder()
                .fullName(savedUser.getUsername())
                .user(savedUser)
                .build();
        profileRepository.save(profile);

        if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        }

        return "User Registered Successfully";
    }

    // ─── LOGIN (Email OR Phone OR Username) ───────────────────────────────────
    public AuthResponse login(String identifier, String password) {

        User user = userRepository
                .findByEmailOrPhoneNumberOrUsername(identifier, identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        String name = user.getProfile() != null ? user.getProfile().getFullName() : "";

        return new AuthResponse(token, name, user.getRole().name(), user.getUsername());
    }

    // ─── GOOGLE LOGIN ─────────────────────────────────────────────────────────
    public AuthResponse googleLogin(String email, String name, String googleId, String picture) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, name, googleId, picture));

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        String fullName = user.getProfile() != null ? user.getProfile().getFullName() : name;

        return new AuthResponse(token, fullName, user.getRole().name(), user.getUsername());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private User createGoogleUser(String email, String name, String googleId, String picture) {

        User user = new User();
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);

        // Generate unique username from email prefix
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        if (baseUsername.isBlank()) {
            baseUsername = "user";
        }
        user.setUsername(generateUniqueUsername(baseUsername));

        // ✅ NULL not "" — MySQL allows multiple NULLs in a UNIQUE column
        user.setPhoneNumber(null);

        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .fullName(name != null ? name : baseUsername)
                .user(savedUser)
                .build();
        profileRepository.save(profile);

        emailService.sendWelcomeEmail(email, name != null ? name : baseUsername);

        return savedUser;
    }

    // Ensures username is unique — e.g. "sarah" → "sarah2" → "sarah3"
    private String generateUniqueUsername(String base) {
        String username = base;
        int counter = 2;
        while (userRepository.existsByUsername(username)) {
            username = base + counter;
            counter++;
        }
        return username;
    }
}