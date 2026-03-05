package com.app.shecare.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shecare.entity.Profile;
import com.app.shecare.entity.Role;
import com.app.shecare.entity.User;
import com.app.shecare.repository.ProfileRepository;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.security.JwtService;
import com.app.shecare.dto.AuthResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    // ✅ REGISTER USER
    public String register(User request) {

    // Email or Phone must be provided
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

    emailService.sendWelcomeEmail(savedUser.getEmail());

    return "User Registered Successfully";
}
    // ✅ LOGIN (Email OR Phone)
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

    return new AuthResponse(
            token,
            name,
            user.getRole().name(),
            user.getUsername()
    );
}
}