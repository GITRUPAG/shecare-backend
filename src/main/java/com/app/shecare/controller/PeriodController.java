package com.app.shecare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shecare.dto.CycleCalendarResponse;
import com.app.shecare.dto.HealthAlert;
import com.app.shecare.dto.PcosPredictionRequest;
import com.app.shecare.dto.PcosPredictionResponse;
import com.app.shecare.dto.PcosSymptomRequest;
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

    // 🔹 Create new period log
    @PostMapping("/log")
    public Object logPeriod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PeriodLogRequest request
    ) {
        return periodService.addPeriodLog(userDetails.getUser(), request);
    }

    // ✅ Edit an existing period log by ID
    @PutMapping("/log/{id}")
    public ResponseEntity<?> editPeriodLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody PeriodLogRequest request
    ) {
        try {
            Object result = periodService.updatePeriodLog(userDetails.getUser(), id, request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // ✅ Delete a period log by ID
    @DeleteMapping("/log/{id}")
    public ResponseEntity<?> deletePeriodLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        try {
            periodService.deletePeriodLog(userDetails.getUser(), id);
            return ResponseEntity.ok(java.util.Map.of("message", "Period log deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
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
        PcosPredictionRequest request = periodService.buildPcosRequest(userDetails.getUser());
        return aiPredictionService.predictPcos(request);
    }

    @GetMapping("/logs")
    public List<PeriodLog> getLogs(@AuthenticationPrincipal CustomUserDetails ud) {
        return periodService.getPeriodLogs(ud.getUser());
    }

    @PostMapping("/pcos/symptoms")
public ResponseEntity<?> savePcosSymptoms(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PcosSymptomRequest symptoms
) {
    try {
        periodService.savePcosSymptoms(userDetails.getUser(), symptoms);
        return ResponseEntity.ok(java.util.Map.of("message", "Symptoms saved successfully."));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    }
}
}