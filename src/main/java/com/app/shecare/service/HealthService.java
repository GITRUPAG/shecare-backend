package com.app.shecare.service;

import com.app.shecare.dto.BmiResponse;
import com.app.shecare.dto.HealthRiskResponse;
import com.app.shecare.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.app.shecare.dto.DailyInsightResponse;
import com.app.shecare.dto.CycleCalendarResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final ProfileService profileService;
    private final PeriodService periodService;

    public HealthRiskResponse calculateRisk(User user) {

        // Null-safe BMI — returns default score if profile incomplete
        BmiResponse bmiResponse = profileService.calculateBmi(user);

        int score = 100;
        List<String> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (bmiResponse == null) {
            // Profile incomplete — return a friendly default response
            factors.add("Complete your profile to get personalized health insights");
            recommendations.add("Add your height and weight in the Profile section");
            return HealthRiskResponse.builder()
                    .healthScore(score)
                    .riskLevel("Unknown")
                    .pcosRisk("low")
                    .factors(factors)
                    .recommendations(recommendations)
                    .build();
        }

        double bmi = bmiResponse.getBmi();

        // Null-safe age
        Integer age = (user.getProfile() != null) ? user.getProfile().getAge() : null;

        // ── Health Score ──────────────────────────────────────────────
        if (bmi > 25) {
            score -= 15;
            factors.add("BMI above normal range");
            recommendations.add("Maintain balanced diet and regular exercise");
        }

        if (bmi < 18.5) {
            score -= 10;
            factors.add("BMI below healthy range");
            recommendations.add("Improve nutrition intake");
        }

        if (age != null && age > 35) {
            score -= 5;
            factors.add("Age above 35 may affect cycle regularity");
        }

        String riskLevel;
        if (score >= 80) {
            riskLevel = "Low";
        } else if (score >= 60) {
            riskLevel = "Moderate";
        } else {
            riskLevel = "High";
        }

        // ── PCOS Risk ─────────────────────────────────────────────────
        int pcosScore = 0;

        // High BMI is a strong PCOS indicator
        if (bmi > 27) {
            pcosScore += 2;
        } else if (bmi > 25) {
            pcosScore += 1;
        }

        // Age factor
        if (age != null && age >= 18 && age <= 35) {
            // PCOS is most common in reproductive age
            pcosScore += 1;
        }

        if (age != null && age > 35) {
            pcosScore += 1;
        }

        // Underweight can also be a signal
        if (bmi < 18.5) {
            pcosScore += 1;
        }

        String pcosRisk;
        if (pcosScore >= 3) {
            pcosRisk = "high";
            factors.add("Multiple PCOS risk indicators detected");
            recommendations.add("Consider consulting a gynaecologist for PCOS screening");
        } else if (pcosScore >= 1) {
            pcosRisk = "medium";
            recommendations.add("Monitor your cycle regularly for any irregularities");
        } else {
            pcosRisk = "low";
        }

        return HealthRiskResponse.builder()
                .healthScore(score)
                .riskLevel(riskLevel)
                .pcosRisk(pcosRisk)
                .factors(factors)
                .recommendations(recommendations)
                .build();
    }

    public DailyInsightResponse getDailyInsight(User user) {

        CycleCalendarResponse calendar = periodService.getCycleCalendar(user);

        String phase = calendar.getCurrentPhase();

        String energy;
        String recommendation;
        String tip;

        switch (phase) {
            case "Menstrual":
                energy = "Low";
                recommendation = "Rest, light stretching or yoga";
                tip = "Your body is renewing itself. Rest is important.";
                break;
            case "Follicular":
                energy = "Rising";
                recommendation = "Great time for workouts and new projects";
                tip = "Estrogen rising boosts creativity and focus.";
                break;
            case "Ovulation":
                energy = "Peak";
                recommendation = "Social activities and important meetings";
                tip = "Your confidence and communication are strongest.";
                break;
            case "Luteal":
                energy = "Declining";
                recommendation = "Focus on planning and lighter workouts";
                tip = "Take time to slow down and prioritize sleep.";
                break;
            default:
                energy = "Normal";
                recommendation = "Maintain healthy habits";
                tip = "Listen to your body.";
        }

        return DailyInsightResponse.builder()
                .phase(phase)
                .energyLevel(energy)
                .recommendation(recommendation)
                .tip(tip)
                .build();
    }
}