package com.app.shecare.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shecare.dto.CycleCalendarResponse;
import com.app.shecare.dto.HealthAlert;
import com.app.shecare.dto.PcosPredictionRequest;
import com.app.shecare.dto.PcosPredictionResponse;
import com.app.shecare.dto.PeriodLogRequest;
import com.app.shecare.dto.PeriodPredictionRequest;
import com.app.shecare.entity.PeriodLog;
import com.app.shecare.entity.PeriodPrediction;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.AiPredictionService;
import com.app.shecare.service.PeriodService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/period")
@RequiredArgsConstructor
public class PeriodController {

    private final AiPredictionService aiPredictionService;

    private final PeriodService periodService;

    // 🔹 Predict cycle from period dates
    @PostMapping("/predict")
    public Object predictCycle(@RequestBody PeriodPredictionRequest request) {

        return aiPredictionService.predictFromDates(request);
    }

    @PostMapping("/log")
public Object logPeriod(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PeriodLogRequest request
) {

    return periodService.addPeriodLog(userDetails.getUser(), request);

}

@GetMapping("/prediction")
public PeriodPrediction getPrediction(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return periodService.getLatestPrediction(userDetails.getUser());

}

@GetMapping("/phase-insights")
public Object getPhaseInsights(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return periodService.getPhaseInsights(userDetails.getUser());

}

@GetMapping("/alerts")
public List<HealthAlert> getAlerts(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return periodService.getHealthAlerts(userDetails.getUser());

}

@GetMapping("/calendar")
public CycleCalendarResponse getCalendar(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return periodService.getCycleCalendar(userDetails.getUser());

}

@GetMapping("/pcos")
public PcosPredictionResponse predictPcos(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    PcosPredictionRequest request =
            periodService.buildPcosRequest(userDetails.getUser());

    return aiPredictionService.predictPcos(request);
}

@GetMapping("/logs")
public List<PeriodLog> getLogs(@AuthenticationPrincipal CustomUserDetails ud) {
    return periodService.getPeriodLogs(ud.getUser());
}
}