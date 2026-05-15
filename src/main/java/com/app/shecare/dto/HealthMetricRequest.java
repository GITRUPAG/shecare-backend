package com.app.shecare.dto;
 
import java.math.BigDecimal;
import java.time.Instant;

import com.app.shecare.entity.HealthMetric.MetricType;

import jakarta.validation.constraints.NotNull;
 
// ─── Request ─────────────────────────────────────────────────────────────────
 
public record HealthMetricRequest(
        @NotNull MetricType type,
        @NotNull BigDecimal value,
        BigDecimal valueSecondary,   // nullable — only used for BLOOD_PRESSURE
        @NotNull Instant recordedAt,
        String source                // "BLE" | "APPLE_HEALTH" | "HEALTH_CONNECT"
) {}