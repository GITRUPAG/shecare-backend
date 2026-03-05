package com.app.shecare.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.app.shecare.dto.CycleCalendarResponse;
import com.app.shecare.dto.HealthAlert;
import com.app.shecare.dto.PcosPredictionRequest;
import com.app.shecare.dto.PeriodLogRequest;
import com.app.shecare.entity.PeriodLog;
import com.app.shecare.entity.PeriodPrediction;
import com.app.shecare.entity.User;
import com.app.shecare.repository.PeriodLogRepository;
import com.app.shecare.repository.PeriodPredictionRepository;
import com.app.shecare.repository.ProfileRepository;
import com.app.shecare.dto.PcosPredictionResponse;
import com.app.shecare.dto.ToxicityResponse;
import com.app.shecare.dto.PeriodPredictionRequest;
import com.app.shecare.dto.PeriodLogRequest;
import com.app.shecare.service.ProfileService;
import com.app.shecare.entity.Profile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeriodService {

    private final PeriodLogRepository periodLogRepository;
    private final PeriodPredictionRepository predictionRepository;
    private final AiPredictionService aiPredictionService;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

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

        // 🔹 Fetch last 6 logs
        List<PeriodLog> logs =
                periodLogRepository.findTop6ByUserOrderByStartDateDesc(user);

        if (logs.size() < 2) {
            return log; // not enough data for prediction
        }

        // 🔹 Build AI request
        List<Map<String, String>> periods = new ArrayList<>();

        for (PeriodLog l : logs) {

            Map<String, String> entry = new HashMap<>();
            entry.put("start", l.getStartDate().toString());
            entry.put("end", l.getEndDate().toString());

            periods.add(entry);
        }

        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("periods", periods);
        aiRequest.put("numCycles", 3);

        // 🔹 Call Python API
        Object prediction = aiPredictionService.predictFromDates(aiRequest);

        Map<String, Object> result = (Map<String, Object>) prediction;

// next period
Map<String, Object> nextPeriod =
        (Map<String, Object>) result.get("next_period");

String start = (String) nextPeriod.get("predicted_start");
String end   = (String) nextPeriod.get("predicted_end");

// upcoming cycles
List<Map<String, Object>> cycles =
        (List<Map<String, Object>>) result.get("upcoming_cycles");

Map<String, Object> firstCycle = cycles.get(0);

Map<String, Object> phases =
        (Map<String, Object>) firstCycle.get("phases");

// ovulation
Map<String, Object> ovulation =
        (Map<String, Object>) phases.get("ovulation");

// fertile window
Map<String, Object> fertile =
        (Map<String, Object>) phases.get("fertile_window");

// cycle length
Double cycleLength =
        Double.valueOf(firstCycle.get("predicted_cycle_length").toString());

// build entity
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


        return prediction;
    }

    public PeriodPrediction getLatestPrediction(User user) {

    return predictionRepository
            .findTopByUserOrderByPredictedStartDateDesc(user)
            .orElse(null);
}

public Object getPhaseInsights(User user) {

    PeriodPrediction prediction =
            predictionRepository
                    .findTopByUserOrderByPredictedStartDateDesc(user)
                    .orElseThrow();

    LocalDate today = LocalDate.now();

    String phase = "follicular";

    if (today.isAfter(prediction.getPredictedStartDate())
            && today.isBefore(prediction.getPredictedEndDate())) {

        phase = "menstrual";
    }

    if (today.isAfter(prediction.getFertileStart())
            && today.isBefore(prediction.getFertileEnd())) {

        phase = "ovulation";
    }

    if (today.isAfter(prediction.getOvulationDay())) {

        phase = "luteal";
    }

    Map<String, Object> request = new HashMap<>();

    request.put("phase", phase);
    request.put("age", 25);

    return aiPredictionService.getPhaseInsights(request);
}

public List<HealthAlert> getHealthAlerts(User user) {

    PeriodPrediction prediction =
            predictionRepository
                    .findTopByUserOrderByPredictedStartDateDesc(user)
                    .orElse(null);

    if (prediction == null) return List.of();

    LocalDate today = LocalDate.now();

    List<HealthAlert> alerts = new ArrayList<>();

    long daysToPeriod =
            ChronoUnit.DAYS.between(today, prediction.getPredictedStartDate());

    long daysToOvulation =
            ChronoUnit.DAYS.between(today, prediction.getOvulationDay());

    long daysToFertile =
            ChronoUnit.DAYS.between(today, prediction.getFertileStart());

    // Period alert
    if (daysToPeriod >= 0 && daysToPeriod <= 2) {

        String message;

        if (daysToPeriod == 0) {
            message = "Your period is expected today";
        } else if (daysToPeriod == 1) {
            message = "Your period is expected tomorrow";
        } else {
            message = "Your next period is expected in " + daysToPeriod + " days";
        }

        alerts.add(new HealthAlert(
                "PERIOD",
                message,
                "info"
        ));
    }

    // Ovulation alert
    if (daysToOvulation >= 0 && daysToOvulation <= 2) {

        alerts.add(new HealthAlert(
                "OVULATION",
                "Ovulation expected in " + daysToOvulation + " days",
                "info"
        ));
    }

    // Fertile window alert
    if (daysToFertile >= 0 && daysToFertile <= 1) {

        alerts.add(new HealthAlert(
                "FERTILITY",
                "Your fertile window begins tomorrow",
                "info"
        ));
    }

    return alerts;
}

public CycleCalendarResponse getCycleCalendar(User user) {

    PeriodPrediction prediction =
            predictionRepository
                    .findTopByUserOrderByPredictedStartDateDesc(user)
                    .orElseThrow();

    LocalDate today = LocalDate.now();

    String phase = "Follicular";

    if (today.isAfter(prediction.getPredictedStartDate())
            && today.isBefore(prediction.getPredictedEndDate())) {

        phase = "Menstrual";
    }

    if (today.isAfter(prediction.getFertileStart())
            && today.isBefore(prediction.getFertileEnd())) {

        phase = "Fertile";
    }

    if (today.equals(prediction.getOvulationDay())) {

        phase = "Ovulation";
    }

    if (today.isAfter(prediction.getOvulationDay())) {

        phase = "Luteal";
    }

    LocalDate pmsStart = prediction.getPredictedStartDate().minusDays(5);

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

public List<PeriodLog> getPeriodLogs(User user) {
    return periodLogRepository.findByUserOrderByStartDateDesc(user);
}

public PcosPredictionRequest buildPcosRequest(User user) {

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        PeriodPrediction prediction =
                predictionRepository
                        .findTopByUserOrderByPredictedStartDateDesc(user)
                        .orElseThrow(() -> new RuntimeException("No prediction found"));

        double bmi = calculateBmi(profile);

        return PcosPredictionRequest.builder()
                .age(profile.getAge())
                .bmi(bmi)

                .cycle_length(prediction.getPredictedCycleLength().intValue())
                .cycle_regularity(2)

                .follicle_no_right(5)
                .follicle_no_left(5)

                .amh(3.0)
                .fsh(5.0)
                .lh(7.0)
                .fsh_lh_ratio(0.71)

                .waist_hip_ratio(0.8)
                .endometrium_mm(7.0)

                .avg_follicle_size_r(12.0)
                .avg_follicle_size_l(12.0)

                .weight_gain(0)
                .hair_growth(0)
                .skin_darkening(0)
                .pimples(0)
                .build();
    }

public double calculateBmi(Profile profile) {

    if (profile.getHeight() == null || profile.getWeight() == null) {
        return 22.0; // safe default
    }

    double heightMeters = profile.getHeight() / 100.0;

    return profile.getWeight() / (heightMeters * heightMeters);
}


}