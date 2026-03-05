package com.app.shecare.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.app.shecare.entity.Profile;
import com.app.shecare.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Get logged-in profile
    @GetMapping("/me")
    public Profile getProfile(Authentication authentication) {

        String email = authentication.getName();

        return profileService.getProfile(email);
    }

    // Update profile
    @PutMapping("/me")
    public Profile updateProfile(
            Authentication authentication,
            @RequestBody Profile request) {

        String email = authentication.getName();

        return profileService.updateProfile(email, request);
    }

    // Upload profile picture
    @PostMapping("/upload-image")
    public Profile uploadProfileImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        String email = authentication.getName();

        return profileService.uploadProfileImage(email, file);
    }
}