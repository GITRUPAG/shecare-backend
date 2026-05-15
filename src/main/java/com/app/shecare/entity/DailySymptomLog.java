package com.app.shecare.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "daily_symptom_logs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySymptomLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core date & user ──────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    // ── Mood ──────────────────────────────────────────────────────────────────
    // Values: SAD, LOW, OKAY, GOOD, GREAT
    @Column(length = 20)
    private String mood;

    // ── Flow intensity ────────────────────────────────────────────────────────
    // Values: NONE, SPOTTING, LIGHT, MODERATE, HEAVY
    @Column(length = 20)
    private String flowIntensity;

    // ── Today's metrics (0–10 scale unless noted) ─────────────────────────────
    private Integer painLevel;       // 0–10

    private Integer sleepHours;      // 3–12 hours

    private Integer stressLevel;     // 0–10

    private Integer energyLevel;     // 0–10

    // ── Symptoms (stored as comma-separated string for simplicity) ────────────
    // e.g. "CRAMPS,BLOATING,HEADACHE"
    // Possible values: CRAMPS, BLOATING, HEADACHE, FATIGUE,
    //                  BREAST_TENDERNESS, BACK_PAIN, NAUSEA, ACNE, MOOD_SWINGS
    @Column(length = 500)
    private String symptoms;

    // ── Free-text notes ───────────────────────────────────────────────────────
    @Column(length = 1000)
    private String notes;

    // ── Cycle phase at time of log (snapshot from prediction) ─────────────────
    // Values: Menstrual, Follicular, Ovulation, Fertile, Luteal, PMS, unknown
    @Column(length = 30)
    private String cyclePhase;

    // ── Audit ─────────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}