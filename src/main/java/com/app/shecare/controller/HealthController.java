package com.app.shecare.controller;

import com.app.shecare.dto.BmiResponse;
import com.app.shecare.dto.DailyInsightResponse;
import com.app.shecare.dto.HealthRiskResponse;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.ProfileService;
import com.app.shecare.service.HealthService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final ProfileService profileService;

    private final HealthService healthService;

    @GetMapping("/bmi")
    public BmiResponse getBmi(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return profileService.calculateBmi(userDetails.getUser());

    }

    @GetMapping("/risk")
public HealthRiskResponse getRiskScore(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return healthService.calculateRisk(userDetails.getUser());

}

@GetMapping("/daily-insight")
public DailyInsightResponse getDailyInsight(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return healthService.getDailyInsight(userDetails.getUser());

}
}