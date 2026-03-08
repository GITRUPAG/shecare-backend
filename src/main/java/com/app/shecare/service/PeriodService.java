package com.app.shecare.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.app.shecare.dto.CycleCalendarResponse;
import com.app.shecare.dto.HealthAlert;
import com.app.shecare.dto.PcosPredictionRequest;
import com.app.shecare.dto.PcosSymptomRequest;
import com.app.shecare.dto.PeriodLogRequest;
import com.app.shecare.entity.PeriodLog;
import com.app.shecare.entity.PeriodPrediction;
import com.app.shecare.entity.Profile;
import com.app.shecare.entity.User;
import com.app.shecare.repository.PeriodLogRepository;
import com.app.shecare.repository.PeriodPredictionRepository;
import com.app.shecare.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeriodService {

    private final PeriodLogRepository periodLogRepository;
    private final PeriodPredictionRepository predictionRepository;
    private final AiPredictionService aiPredictionService;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    // ─── Calculate age from profile ───────────────────────────────────────────
    private int calculateAge(Profile profile) {
        // Priority 1: calculate from dateOfBirth (most accurate)
        if (profile.getDateOfBirth() != null) {
            int age = (int) ChronoUnit.YEARS.between(profile.getDateOfBirth(), LocalDate.now());
            if (age > 0 && age <= 80) return age;
        }
        // Priority 2: use stored age field
        if (profile.getAge() != null && profile.getAge() > 0) {
            return profile.getAge();
        }
        // Fallback
        return 25;
    }

    // ─── CREATE new period log ────────────────────────────────────────────────
    public Object addPeriodLog(User user, PeriodLogRequest request) {

        PeriodLog log = PeriodLog.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .duration(
                        (int)(request.getEndDate().toEpochDay() -
                                request.getStartDate().toEpochDay()) + 1
                )
                .user(user)
                .build();

        periodLogRepository.save(log);

        return refreshPrediction(user, log);
    }

    // ─── EDIT existing period log ─────────────────────────────────────────────
    public Object updatePeriodLog(User user, Long id, PeriodLogRequest request) {

        PeriodLog log = periodLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Period log not found."));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to edit this log.");
        }

        log.setStartDate(request.getStartDate());
        log.setEndDate(request.getEndDate());
        log.setDuration(
                (int)(request.getEndDate().toEpochDay() -
                        request.getStartDate().toEpochDay()) + 1
        );

        periodLogRepository.save(log);

        return refreshPrediction(user, log);
    }

    // ─── DELETE period log ────────────────────────────────────────────────────
    public void deletePeriodLog(User user, Long id) {

        PeriodLog log = periodLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Period log not found."));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this log.");
        }

        periodLogRepository.delete(log);

        // Re-run prediction on remaining logs so calendar stays accurate
        List<PeriodLog> remaining = periodLogRepository.findTop6ByUserOrderByStartDateDesc(user);
        if (remaining.size() >= 2) {
            buildAndSavePrediction(user, remaining);
        }
    }

    // ─── Shared: refresh AI prediction after create/edit ─────────────────────
    private Object refreshPrediction(User user, PeriodLog savedLog) {

        List<PeriodLog> logs = periodLogRepository.findTop6ByUserOrderByStartDateDesc(user);

        if (logs.size() < 2) {
            return savedLog; // not enough data yet
        }

        return buildAndSavePrediction(user, logs);
    }

    // ─── Shared: build AI request and persist prediction ─────────────────────
    private Object buildAndSavePrediction(User user, List<PeriodLog> logs) {

        List<PeriodLog> sorted = new ArrayList<>(logs);
    sorted.sort(Comparator.comparing(PeriodLog::getStartDate));

    List<Map<String, String>> periods = new ArrayList<>();
    for (PeriodLog l : sorted) {
        Map<String, String> entry = new HashMap<>();
        entry.put("start", l.getStartDate().toString());
        entry.put("end",   l.getEndDate().toString());
        periods.add(entry);
    }

    Map<String, Object> aiRequest = new HashMap<>();
    aiRequest.put("periods",    periods);
    aiRequest.put("num_cycles", 3);

        Object prediction = aiPredictionService.predictFromDates(aiRequest);

        try {
            Map<String, Object> result     = (Map<String, Object>) prediction;
            Map<String, Object> nextPeriod = (Map<String, Object>) result.get("next_period");

            String start = (String) nextPeriod.get("predicted_start");
            String end   = (String) nextPeriod.get("predicted_end");

            List<Map<String, Object>> cycles = (List<Map<String, Object>>) result.get("upcoming_cycles");
            Map<String, Object> firstCycle   = cycles.get(0);
            Map<String, Object> phases       = (Map<String, Object>) firstCycle.get("phases");
            Map<String, Object> ovulation    = (Map<String, Object>) phases.get("ovulation");
            Map<String, Object> fertile      = (Map<String, Object>) phases.get("fertile_window");
            Double cycleLength               = Double.valueOf(firstCycle.get("predicted_cycle_length").toString());

            PeriodPrediction predictionEntity = PeriodPrediction.builder()
                    .predictedStartDate(LocalDate.parse(start))
                    .predictedEndDate(LocalDate.parse(end))
                    .ovulationDay(LocalDate.parse((String) ovulation.get("day")))
                    .fertileStart(LocalDate.parse((String) fertile.get("start")))
                    .fertileEnd(LocalDate.parse((String) fertile.get("end")))
                    .predictedCycleLength(cycleLength)
                    .user(user)
                    .build();

            predictionRepository.save(predictionEntity);

        } catch (ClassCastException | NullPointerException | IndexOutOfBoundsException e) {
            throw new RuntimeException("AI prediction response format unexpected: " + e.getMessage());
        }

        return prediction;
    }

    // ─── Get latest prediction ────────────────────────────────────────────────
    public PeriodPrediction getLatestPrediction(User user) {
        return predictionRepository
                .findTopByUserOrderByPredictedStartDateDesc(user)
                .orElse(null);
    }

    // ─── Get phase insights ───────────────────────────────────────────────────
    public Object getPhaseInsights(User user) {

        PeriodPrediction prediction = predictionRepository
                .findTopByUserOrderByPredictedStartDateDesc(user)
                .orElse(null);

        if (prediction == null) {
            return Map.of(
                "phase",   "unknown",
                "message", "No cycle data available yet. Please log your period first."
            );
        }

        LocalDate today = LocalDate.now();
        String phase = "follicular";

        if (!today.isBefore(prediction.getPredictedStartDate())
                && !today.isAfter(prediction.getPredictedEndDate())) {
            phase = "menstrual";
        } else if (!today.isBefore(prediction.getFertileStart())
                && !today.isAfter(prediction.getFertileEnd())) {
            phase = "ovulation";
        } else if (today.isAfter(prediction.getOvulationDay())) {
            phase = "luteal";
        }

        // ✅ Real age from profile — calculated from dateOfBirth first, then age field
        Profile profile = profileRepository.findByUser(user).orElse(null);
        int age = profile != null ? calculateAge(profile) : 25;

        Map<String, Object> request = new HashMap<>();
        request.put("phase", phase);
        request.put("age",   age);

        return aiPredictionService.getPhaseInsights(request);
    }

    // ─── Get health alerts ────────────────────────────────────────────────────
    public List<HealthAlert> getHealthAlerts(User user) {

        PeriodPrediction prediction = predictionRepository
                .findTopByUserOrderByPredictedStartDateDesc(user)
                .orElse(null);

        if (prediction == null) return List.of();

        LocalDate today = LocalDate.now();
        List<HealthAlert> alerts = new ArrayList<>();

        long daysToPeriod    = ChronoUnit.DAYS.between(today, prediction.getPredictedStartDate());
        long daysToOvulation = ChronoUnit.DAYS.between(today, prediction.getOvulationDay());
        long daysToFertile   = ChronoUnit.DAYS.between(today, prediction.getFertileStart());

        if (daysToPeriod >= 0 && daysToPeriod <= 2) {
            String message = daysToPeriod == 0 ? "Your period is expected today"
                           : daysToPeriod == 1 ? "Your period is expected tomorrow"
                           : "Your next period is expected in " + daysToPeriod + " days";
            alerts.add(new HealthAlert("PERIOD", message, "info"));
        }

        if (daysToOvulation >= 0 && daysToOvulation <= 2) {
            alerts.add(new HealthAlert("OVULATION",
                    "Ovulation expected in " + daysToOvulation + " days", "info"));
        }

        if (daysToFertile >= 0 && daysToFertile <= 1) {
            alerts.add(new HealthAlert("FERTILITY",
                    "Your fertile window begins tomorrow", "info"));
        }

        return alerts;
    }

    // ─── Get cycle calendar ───────────────────────────────────────────────────
    public CycleCalendarResponse getCycleCalendar(User user) {

        PeriodPrediction prediction = predictionRepository
                .findTopByUserOrderByPredictedStartDateDesc(user)
                .orElse(null);

        if (prediction == null) {
            return CycleCalendarResponse.builder()
                    .currentPhase("unknown")
                    .build();
        }

        LocalDate today    = LocalDate.now();
        LocalDate pmsStart = prediction.getPredictedStartDate().minusDays(5);
        String phase;

        if (!today.isBefore(prediction.getPredictedStartDate())
                && !today.isAfter(prediction.getPredictedEndDate())) {
            phase = "Menstrual";
        } else if (!today.isBefore(prediction.getFertileStart())
                && !today.isAfter(prediction.getFertileEnd())) {
            phase = "Fertile";
        } else if (today.equals(prediction.getOvulationDay())) {
            phase = "Ovulation";
        } else if (today.isAfter(prediction.getOvulationDay())
                && today.isBefore(pmsStart)) {
            phase = "Luteal";
        } else if (!today.isBefore(pmsStart)
                && today.isBefore(prediction.getPredictedStartDate())) {
            phase = "PMS";
        } else {
            phase = "Follicular";
        }

        return CycleCalendarResponse.builder()
                .currentPhase(phase)
                .nextPeriodStart(prediction.getPredictedStartDate().toString())
                .nextPeriodEnd(prediction.getPredictedEndDate().toString())
                .ovulationDay(prediction.getOvulationDay().toString())
                .fertileStart(prediction.getFertileStart().toString())
                .fertileEnd(prediction.getFertileEnd().toString())
                .pmsStart(pmsStart.toString())
                .pmsEnd(prediction.getPredictedStartDate().toString())
                .build();
    }

    // ─── Get all period logs ──────────────────────────────────────────────────
    public List<PeriodLog> getPeriodLogs(User user) {
        return periodLogRepository.findByUserOrderByStartDateDesc(user);
    }

    // ─── Build PCOS request ───────────────────────────────────────────────────
    public PcosPredictionRequest buildPcosRequest(User user) {

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.getHeight() == null || profile.getWeight() == null) {
            throw new RuntimeException(
                "Please complete your profile (height, weight) to run PCOS assessment."
            );
        }

        // ✅ Calculate real age — from dateOfBirth first, then age field
        int age = calculateAge(profile);
        if (age <= 0 || age > 80) {
            throw new RuntimeException(
                "Please complete your profile (date of birth or age) to run PCOS assessment."
            );
        }

        PeriodPrediction prediction = predictionRepository
                .findTopByUserOrderByPredictedStartDateDesc(user)
                .orElseThrow(() -> new RuntimeException(
                    "Please log at least one period cycle to enable PCOS prediction."
                ));

        double bmi         = calculateBmi(profile);
        int    cycleLength = prediction.getPredictedCycleLength() != null
                           ? prediction.getPredictedCycleLength().intValue() : 28;

        return PcosPredictionRequest.builder()
                .age(age)
                .bmi(bmi)
                .cycle_length(cycleLength)
                .cycle_regularity(2)
                .follicle_no_right(5)
                .follicle_no_left(5)
                .amh(profile.getPcosAmh()!= null ? profile.getPcosAmh()  : 3.0)
                .fsh(           profile.getPcosFsh()           != null ? profile.getPcosFsh()           : 5.0)
                .lh(            profile.getPcosLh()            != null ? profile.getPcosLh()            : 7.0)
                .fsh_lh_ratio(0.71)
                .waist_hip_ratio(0.8)
                .endometrium_mm(7.0)
                .avg_follicle_size_r(12.0)
                .avg_follicle_size_l(12.0)
                .weight_gain(   profile.getPcosWeightGain()    != null ? profile.getPcosWeightGain()    : 0)
                .hair_growth(   profile.getPcosHairGrowth()    != null ? profile.getPcosHairGrowth()    : 0)
                .skin_darkening(profile.getPcosSkinDarkening() != null ? profile.getPcosSkinDarkening() : 0)
                .pimples(       profile.getPcosPimples()       != null ? profile.getPcosPimples()       : 0)
                .build();
    }

    // ─── Calculate BMI ────────────────────────────────────────────────────────
    public double calculateBmi(Profile profile) {
        if (profile.getHeight() == null || profile.getWeight() == null) return 22.0;
        double heightMeters = profile.getHeight() / 100.0;
        return profile.getWeight() / (heightMeters * heightMeters);
    }

    public void savePcosSymptoms(User user, PcosSymptomRequest symptoms) {
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found."));
        profile.setPcosWeightGain(symptoms.getWeightGain());
        profile.setPcosHairGrowth(symptoms.getHairGrowth());
        profile.setPcosSkinDarkening(symptoms.getSkinDarkening());
        profile.setPcosPimples(symptoms.getPimples());
        profile.setPcosAmh(symptoms.getAmh());
        profile.setPcosFsh(symptoms.getFsh());
        profile.setPcosLh(symptoms.getLh());
        profile.setPcosWaistHipRatio(symptoms.getWaistHipRatio());
        profileRepository.save(profile);
}
}