package com.app.shecare.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.shecare.dto.BmiResponse;
import com.app.shecare.entity.Profile;
import com.app.shecare.entity.User;
import com.app.shecare.repository.ProfileRepository;
import com.app.shecare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // Get logged-in user profile
    public Profile getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    // Update profile details
    public Profile updateProfile(String email, Profile request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setAge(request.getAge());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setCountry(request.getCountry());

        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setBloodGroup(request.getBloodGroup());

        profile.setSmoker(request.getSmoker());
        profile.setAlcoholic(request.getAlcoholic());
        profile.setActivityLevel(request.getActivityLevel());

        profile.setHasPCOS(request.getHasPCOS());
        profile.setHasDiabetes(request.getHasDiabetes());
        profile.setHasThyroid(request.getHasThyroid());
        profile.setHasHypertension(request.getHasHypertension());

        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactNumber(request.getEmergencyContactNumber());

        profile.setNotificationsEnabled(request.getNotificationsEnabled());
        profile.setDarkModeEnabled(request.getDarkModeEnabled());

        return profileRepository.save(profile);
    }

    // Upload profile image
    public Profile uploadProfileImage(String email, MultipartFile file) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String imageUrl = cloudinaryService.uploadImage(file);

        profile.setProfileImageUrl(imageUrl);

        return profileRepository.save(profile);
    }

    public BmiResponse calculateBmi(User user) {

    double heightCm = user.getProfile().getHeight();
    double weightKg = user.getProfile().getWeight();

    double heightMeters = heightCm / 100;

    double bmi = weightKg / (heightMeters * heightMeters);

    String category;

    if (bmi < 18.5) {
        category = "Underweight";
    } else if (bmi < 25) {
        category = "Normal";
    } else if (bmi < 30) {
        category = "Overweight";
    } else {
        category = "Obese";
    }

    return BmiResponse.builder()
            .bmi(Math.round(bmi * 10.0) / 10.0)
            .category(category)
            .build();
}

public int calculateAge(LocalDate dob) {
    return Period.between(dob, LocalDate.now()).getYears();
}

}