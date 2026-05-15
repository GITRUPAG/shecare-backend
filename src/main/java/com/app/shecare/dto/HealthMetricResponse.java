package com.app.shecare.dto;

import com.app.shecare.entity.HealthMetric;
import com.app.shecare.entity.HealthMetric.MetricType;

import java.time.Instant;
import java.util.UUID;
import java.math.BigDecimal;

public record HealthMetricResponse(
        Long id,
        MetricType type,
        BigDecimal value,
        BigDecimal valueSecondary,
        Instant recordedAt,
        String source
) {
    public static HealthMetricResponse from(HealthMetric m) {
        return new HealthMetricResponse(
                m.getId(),
                m.getType(),
                m.getValue(),
                m.getValueSecondary(),
                m.getRecordedAt(),
                m.getSource()
        );
    }
}
