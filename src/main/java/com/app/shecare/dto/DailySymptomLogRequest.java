// ─────────────────────────────────────────────────────────────────────────────
// FILE 1: DailySymptomLogRequest.java
// ─────────────────────────────────────────────────────────────────────────────
package com.app.shecare.dto;
 
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
 
@Data
public class DailySymptomLogRequest {
 
    /**
     * Date this log is for. Defaults to today if null.
     * Allows backdating (e.g. logging yesterday's symptoms).
     */
    private LocalDate logDate;
 
    // ── Mood: SAD | LOW | OKAY | GOOD | GREAT ────────────────────────────────
    private String mood;
 
    // ── Flow: NONE | SPOTTING | LIGHT | MODERATE | HEAVY ─────────────────────
    private String flowIntensity;
 
    // ── Metrics ───────────────────────────────────────────────────────────────
    private Integer painLevel;    // 0–10
    private Integer sleepHours;   // 3–12
    private Integer stressLevel;  // 0–10
    private Integer energyLevel;  // 0–10
 
    /**
     * List of selected symptom chips from the UI.
     * Accepted values: CRAMPS, BLOATING, HEADACHE, FATIGUE,
     *                  BREAST_TENDERNESS, BACK_PAIN, NAUSEA, ACNE, MOOD_SWINGS
     */
    private List<String> symptoms;
 
    // ── Free-text notes ───────────────────────────────────────────────────────
    private String notes;
}