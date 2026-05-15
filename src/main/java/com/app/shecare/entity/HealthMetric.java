package com.app.shecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_metrics", indexes = {
    @Index(name = "idx_hm_user_type_time", columnList = "user_id, type, recorded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Matches User.id which is Long (GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetricType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    // Used for blood pressure — value = systolic, valueSecondary = diastolic
    @Column(name = "value_secondary", precision = 10, scale = 2)
    private BigDecimal valueSecondary;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    // "BLE" | "APPLE_HEALTH" | "HEALTH_CONNECT"
    @Column(length = 30)
    private String source;

    public enum MetricType {
        HEART_RATE, STEPS, BLOOD_PRESSURE, SPO2
    }
}