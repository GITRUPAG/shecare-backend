// ─────────────────────────────────────────────────────────────────────────────
// FILE 2: DailySymptomLogResponse.java
// ─────────────────────────────────────────────────────────────────────────────
package com.app.shecare.dto;
 
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;
 
@Data
@Builder
public class DailySymptomLogResponse {
 
    private Long id;
    private LocalDate logDate;
 
    // ── Phase context ─────────────────────────────────────────────────────────
    private String cyclePhase;           // e.g. "Menstrual", "Follicular", etc.
    private String phaseEmoji;           // e.g. "🩸" "🌸" "🌕" etc. (frontend convenience)
 
    // ── Mood & flow ───────────────────────────────────────────────────────────
    private String mood;
    private String flowIntensity;
 
    // ── Metrics ───────────────────────────────────────────────────────────────
    private Integer painLevel;
    private Integer sleepHours;
    private Integer stressLevel;
    private Integer energyLevel;
 
    // ── Symptoms as a list (deserialized from stored comma string) ────────────
    private List<String> symptoms;
 
    // ── Notes ─────────────────────────────────────────────────────────────────
    private String notes;
 
    // ── Audit ─────────────────────────────────────────────────────────────────
    private String createdAt;
    private String updatedAt;
}