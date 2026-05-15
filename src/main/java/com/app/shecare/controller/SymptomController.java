package com.app.shecare.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shecare.dto.DailySymptomLogRequest;
import com.app.shecare.dto.DailySymptomLogResponse;
import com.app.shecare.dto.SymptomHistoryResponse;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.SymptomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @PostMapping("/log")
    public ResponseEntity<DailySymptomLogResponse> saveLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DailySymptomLogRequest request) {

        DailySymptomLogResponse response = symptomService.saveSymptomLog(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<DailySymptomLogResponse> getToday(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(symptomService.getSymptomLog(userDetails.getUser(), LocalDate.now()));
    }

    @GetMapping("/date")
    public ResponseEntity<DailySymptomLogResponse> getByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(symptomService.getSymptomLog(userDetails.getUser(), date));
    }

    @GetMapping("/history")
    public ResponseEntity<SymptomHistoryResponse> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer days) {

        return ResponseEntity.ok(symptomService.getHistory(userDetails.getUser(), days));
    }

    @GetMapping("/phase")
    public ResponseEntity<List<DailySymptomLogResponse>> getByPhase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String phase) {

        return ResponseEntity.ok(symptomService.getLogsByPhase(userDetails.getUser(), phase));
    }

    @GetMapping("/current-phase")
    public ResponseEntity<Map<String, String>> getCurrentPhase(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String phase = symptomService.resolveCyclePhase(userDetails.getUser(), LocalDate.now());
        return ResponseEntity.ok(Map.of("phase", phase));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        symptomService.deleteSymptomLog(userDetails.getUser(), id);
        return ResponseEntity.ok(Map.of("message", "Symptom log deleted successfully."));
    }
}   