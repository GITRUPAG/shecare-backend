package com.app.shecare.service;

import com.app.shecare.dto.DailySymptomLogRequest;
import com.app.shecare.dto.DailySymptomLogResponse;
import com.app.shecare.dto.SymptomHistoryResponse;
import com.app.shecare.entity.DailySymptomLog;
import com.app.shecare.entity.PeriodPrediction;
import com.app.shecare.entity.User;
import com.app.shecare.repository.DailySymptomLogRepository;
import com.app.shecare.repository.PeriodPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final DailySymptomLogRepository symptomLogRepository;
    private final PeriodPredictionRepository predictionRepository;

    // ─── Enum-like constants for phase emojis ─────────────────────────────────
    private static final Map<String, String> PHASE_EMOJI = Map.of(
        "Menstrual",  "🩸",
        "Follicular", "🌸",
        "Fertile",    "💚",
        "Ovulation",  "🌕",
        "Luteal",     "🌙",
        "PMS",        "⚡",
        "unknown",    "❓"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE or UPDATE — upsert by (user + logDate)
    // Called from: standalone form, post-period log, daily tracker
    // ─────────────────────────────────────────────────────────────────────────
    public DailySymptomLogResponse saveSymptomLog(User user, DailySymptomLogRequest request) {

        LocalDate logDate = request.getLogDate() != null
                          ? request.getLogDate()
                          : LocalDate.now();

        // Resolve cycle phase at the time of logging
        String cyclePhase = resolveCyclePhase(user, logDate);

        // Upsert: update existing record for that date, or create new
        DailySymptomLog log = symptomLogRepository
                .findByUserAndLogDate(user, logDate)
                .orElse(DailySymptomLog.builder()
                        .user(user)
                        .logDate(logDate)
                        .build());

        log.setMood(request.getMood());
        log.setFlowIntensity(request.getFlowIntensity());
        log.setPainLevel(request.getPainLevel());
        log.setSleepHours(request.getSleepHours());
        log.setStressLevel(request.getStressLevel());
        log.setEnergyLevel(request.getEnergyLevel());
        log.setNotes(request.getNotes());
        log.setCyclePhase(cyclePhase);

        // Convert list → comma-separated string for storage
        if (request.getSymptoms() != null && !request.getSymptoms().isEmpty()) {
            log.setSymptoms(String.join(",", request.getSymptoms()));
        } else {
            log.setSymptoms(null);
        }

        symptomLogRepository.save(log);

        return toResponse(log);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET single day log (today or specific date)
    // ─────────────────────────────────────────────────────────────────────────
    public DailySymptomLogResponse getSymptomLog(User user, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();

        return symptomLogRepository
                .findByUserAndLogDate(user, target)
                .map(this::toResponse)
                .orElseGet(() -> {
                    // Return an empty response pre-filled with today's phase
                    // so the frontend can show the right phase context
                    String phase = resolveCyclePhase(user, target);
                    return DailySymptomLogResponse.builder()
                            .logDate(target)
                            .cyclePhase(phase)
                            .phaseEmoji(PHASE_EMOJI.getOrDefault(phase, "❓"))
                            .symptoms(List.of())
                            .build();
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET full history with aggregated summary
    // ─────────────────────────────────────────────────────────────────────────
    public SymptomHistoryResponse getHistory(User user, Integer days) {

        List<DailySymptomLog> logs;

        if (days != null && days > 0) {
            LocalDate from = LocalDate.now().minusDays(days);
            logs = symptomLogRepository
                    .findByUserAndLogDateBetweenOrderByLogDateDesc(user, from, LocalDate.now());
        } else {
            logs = symptomLogRepository.findByUserOrderByLogDateDesc(user);
        }

        List<DailySymptomLogResponse> responses = logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // ── Symptom frequency map ─────────────────────────────────────────────
        Map<String, Long> frequency = logs.stream()
                .filter(l -> l.getSymptoms() != null && !l.getSymptoms().isBlank())
                .flatMap(l -> Arrays.stream(l.getSymptoms().split(",")))
                .collect(Collectors.groupingBy(s -> s.trim(), Collectors.counting()));

        // ── Average metrics ───────────────────────────────────────────────────
        OptionalDouble avgPain    = logs.stream().filter(l -> l.getPainLevel()   != null)
                                        .mapToInt(DailySymptomLog::getPainLevel).average();
        OptionalDouble avgSleep   = logs.stream().filter(l -> l.getSleepHours()  != null)
                                        .mapToInt(DailySymptomLog::getSleepHours).average();
        OptionalDouble avgStress  = logs.stream().filter(l -> l.getStressLevel() != null)
                                        .mapToInt(DailySymptomLog::getStressLevel).average();
        OptionalDouble avgEnergy  = logs.stream().filter(l -> l.getEnergyLevel() != null)
                                        .mapToInt(DailySymptomLog::getEnergyLevel).average();

        return SymptomHistoryResponse.builder()
                .logs(responses)
                .symptomFrequency(frequency)
                .avgPainLevel(  avgPain.isPresent()   ? Math.round(avgPain.getAsDouble()   * 10.0) / 10.0 : null)
                .avgSleepHours( avgSleep.isPresent()  ? Math.round(avgSleep.getAsDouble()  * 10.0) / 10.0 : null)
                .avgStressLevel(avgStress.isPresent() ? Math.round(avgStress.getAsDouble() * 10.0) / 10.0 : null)
                .avgEnergyLevel(avgEnergy.isPresent() ? Math.round(avgEnergy.getAsDouble() * 10.0) / 10.0 : null)
                .totalLogs(logs.size())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET logs filtered by cycle phase (e.g. all "Menstrual" phase logs)
    // Useful for phase-based pattern insights
    // ─────────────────────────────────────────────────────────────────────────
    public List<DailySymptomLogResponse> getLogsByPhase(User user, String phase) {
        return symptomLogRepository
                .findByUserAndCyclePhaseOrderByLogDateDesc(user, phase)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE a log by ID
    // ─────────────────────────────────────────────────────────────────────────
    public void deleteSymptomLog(User user, Long id) {
        DailySymptomLog log = symptomLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Symptom log not found."));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this log.");
        }

        symptomLogRepository.delete(log);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL: Resolve current cycle phase for a given date
    // Mirrors the logic in PeriodService.getCycleCalendar() exactly
    // ─────────────────────────────────────────────────────────────────────────
    public String resolveCyclePhase(User user, LocalDate date) {
        PeriodPrediction prediction = predictionRepository
                .findTopByUserOrderByPredictedStartDateDesc(user)
                .orElse(null);

        if (prediction == null) return "unknown";

        LocalDate pmsStart = prediction.getPredictedStartDate().minusDays(5);

        if (!date.isBefore(prediction.getPredictedStartDate())
                && !date.isAfter(prediction.getPredictedEndDate())) {
            return "Menstrual";
        } else if (!date.isBefore(prediction.getFertileStart())
                && !date.isAfter(prediction.getFertileEnd())) {
            return "Fertile";
        } else if (date.equals(prediction.getOvulationDay())) {
            return "Ovulation";
        } else if (date.isAfter(prediction.getOvulationDay())
                && date.isBefore(pmsStart)) {
            return "Luteal";
        } else if (!date.isBefore(pmsStart)
                && date.isBefore(prediction.getPredictedStartDate())) {
            return "PMS";
        } else {
            return "Follicular";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL: Entity → Response mapper
    // ─────────────────────────────────────────────────────────────────────────
    private DailySymptomLogResponse toResponse(DailySymptomLog log) {
        List<String> symptomList = (log.getSymptoms() != null && !log.getSymptoms().isBlank())
                ? Arrays.asList(log.getSymptoms().split(","))
                : List.of();

        String phase = log.getCyclePhase() != null ? log.getCyclePhase() : "unknown";

        return DailySymptomLogResponse.builder()
                .id(log.getId())
                .logDate(log.getLogDate())
                .cyclePhase(phase)
                .phaseEmoji(PHASE_EMOJI.getOrDefault(phase, "❓"))
                .mood(log.getMood())
                .flowIntensity(log.getFlowIntensity())
                .painLevel(log.getPainLevel())
                .sleepHours(log.getSleepHours())
                .stressLevel(log.getStressLevel())
                .energyLevel(log.getEnergyLevel())
                .symptoms(symptomList)
                .notes(log.getNotes())
                .createdAt(log.getCreatedAt() != null
                        ? log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(log.getUpdatedAt() != null
                        ? log.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }
}