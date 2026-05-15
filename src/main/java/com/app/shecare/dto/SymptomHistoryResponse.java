// ─────────────────────────────────────────────────────────────────────────────
// FILE 3: SymptomHistoryResponse.java  (wrapper for history + summary)
// ─────────────────────────────────────────────────────────────────────────────
package com.app.shecare.dto;
 
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;
 
@Data
@Builder
public class SymptomHistoryResponse {
 
    private List<DailySymptomLogResponse> logs;
 
    /** Top symptoms ranked by frequency across all logs */
    private Map<String, Long> symptomFrequency;
 
    /** Average metrics across returned logs */
    private Double avgPainLevel;
    private Double avgSleepHours;
    private Double avgStressLevel;
    private Double avgEnergyLevel;
 
    private int totalLogs;
}