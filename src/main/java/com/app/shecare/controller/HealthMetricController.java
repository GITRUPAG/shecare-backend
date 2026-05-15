package com.app.shecare.controller;

import com.app.shecare.dto.HealthMetricRequest;
import com.app.shecare.dto.HealthMetricResponse;
import com.app.shecare.entity.HealthMetric.MetricType;
import com.app.shecare.entity.User;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.service.HealthMetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/health/metrics")
@RequiredArgsConstructor
public class HealthMetricController {

    private final HealthMetricService service;
    private final UserRepository userRepository;

    /**
     * POST /api/v1/health/metrics/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Void> saveBatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid List<HealthMetricRequest> requests) {

        service.saveBatch(resolveUserId(userDetails), requests);
        return ResponseEntity.accepted().build();
    }

    /**
     * GET /api/v1/health/metrics/all
     * Must be declared BEFORE the root @GetMapping below,
     * otherwise Spring matches "all" as the ?type= param.
     */
    @GetMapping("/all")
    public ResponseEntity<List<HealthMetricResponse>> getAllMetrics(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(service.getAll(resolveUserId(userDetails)));
    }

    /**
     * GET /api/v1/health/metrics?type=HEART_RATE&from=...&to=...
     * from/to default to last 24 hours if not provided.
     */
    @GetMapping
    public ResponseEntity<Page<HealthMetricResponse>> getMetrics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam MetricType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Instant resolvedTo   = (to   != null) ? to   : Instant.now();
        Instant resolvedFrom = (from != null) ? from : resolvedTo.minus(24, ChronoUnit.HOURS);

        PageRequest pageable = PageRequest.of(page, size, Sort.by("recordedAt").descending());
        return ResponseEntity.ok(
            service.query(resolveUserId(userDetails), type, resolvedFrom, resolvedTo, pageable)
        );
    }

    // JWT sub = email → look up User → return Long id
    private Long resolveUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return user.getId();
    }
}