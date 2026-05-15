package com.app.shecare.repository;

import com.app.shecare.entity.DailySymptomLog;
import com.app.shecare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySymptomLogRepository extends JpaRepository<DailySymptomLog, Long> {

    // ── Fetch a single day's log ───────────────────────────────────────────────
    Optional<DailySymptomLog> findByUserAndLogDate(User user, LocalDate logDate);

    // ── Full history, newest first ─────────────────────────────────────────────
    List<DailySymptomLog> findByUserOrderByLogDateDesc(User user);

    // ── Range query (e.g. last 30 days, or current cycle window) ──────────────
    List<DailySymptomLog> findByUserAndLogDateBetweenOrderByLogDateDesc(
            User user, LocalDate from, LocalDate to);

    // ── Logs for a specific cycle phase (for phase-based insights) ────────────
    List<DailySymptomLog> findByUserAndCyclePhaseOrderByLogDateDesc(
            User user, String cyclePhase);

    // ── Check if a log already exists for a given date ────────────────────────
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    // ── Symptom frequency across all logs — useful for PCOS correlation ───────
    @Query("""
        SELECT d.symptoms
        FROM DailySymptomLog d
        WHERE d.user = :user
          AND d.symptoms IS NOT NULL
          AND d.symptoms <> ''
        ORDER BY d.logDate DESC
    """)
    List<String> findAllSymptomStringsByUser(@Param("user") User user);

    // ── Latest N logs ─────────────────────────────────────────────────────────
    List<DailySymptomLog> findTop30ByUserOrderByLogDateDesc(User user);
}