package com.app.shecare.scheduler;

import com.app.shecare.dto.HealthAlert;
import com.app.shecare.entity.User;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.service.NotificationService;
import com.app.shecare.service.PeriodService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final UserRepository userRepository;
    private final PeriodService periodService;
    private final NotificationService notificationService;

    // ─── Daily health notifications at 9 AM ───────────────────────────────────
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyNotifications() {
        log.info("🔔 Starting daily notification job...");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                List<HealthAlert> alerts = periodService.getHealthAlerts(user);
                if (alerts == null || alerts.isEmpty()) continue;

                for (HealthAlert alert : alerts) {
                    NotifCopy copy = buildCopy(alert);
                    if (copy == null) continue;

                    notificationService.sendToUser(
                            user.getId(),
                            copy.title(),
                            copy.body(),
                            alert.getType(),
                            resolveScreen(alert.getType())
                    );

                    log.info("📩 Sent {} notification to user {} (days={})",
                            alert.getType(), user.getId(), alert.getDaysUntil());
                }

            } catch (Exception e) {
                log.error("❌ Error sending notifications for user {}: {}",
                        user.getId(), e.getMessage());
            }
        }

        log.info("✅ Daily notification job completed");
    }

    // ─── Cara AI weekly insight at 10 AM every Monday ─────────────────────────
    /**
     * Every Monday morning, Cara generates a personalised weekly insight
     * for each user based on their cycle and health logs.
     * Replace the body string with your actual AiPredictionService call.
     */
    @Scheduled(cron = "0 0 10 * * MON")
    public void sendCaraWeeklyInsights() {
        log.info("🤖 Starting Cara AI weekly insight job...");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                // TODO: replace with real insight from AiPredictionService
                // String insight = aiPredictionService.generateWeeklyInsight(user);
                String insight = buildCaraInsightBody(user);

                notificationService.sendCaraInsight(user.getId(), insight);

                log.info("🤖 Cara insight sent to user {}", user.getId());
            } catch (Exception e) {
                log.error("❌ Cara insight failed for user {}: {}",
                        user.getId(), e.getMessage());
            }
        }

        log.info("✅ Cara AI weekly insight job completed");
    }

    // ─── Copy builder — multi-message cadence ─────────────────────────────────
    /**
     * Returns the right title + body based on alert type and daysUntil.
     * HealthAlert must expose getDaysUntil() — add that field if missing.
     */
    private NotifCopy buildCopy(HealthAlert alert) {
        int days = alert.getDaysUntil(); // number of days until event

        return switch (alert.getType()) {

            case "PERIOD" -> switch (days) {
                case 5 -> new NotifCopy(
                        "She's almost here 🩸",
                        "Your period is expected in 5 days. Stock up on your essentials and take it easy — you've got this."
                );
                case 2 -> new NotifCopy(
                        "Cramps inbound? Be ready 💊",
                        "Just 2 days to go. Have your heating pad, snacks, and self-care plan ready. We're rooting for you."
                );
                case 0 -> new NotifCopy(
                        "Period day 1 — be gentle with yourself 🩸",
                        "Your period starts today. Rest, hydrate, and give yourself all the grace. You're doing great."
                );
                default -> null; // no notification for other days
            };

            case "OVULATION" -> switch (days) {
                case 1 -> new NotifCopy(
                        "Tomorrow is your peak day 🌸",
                        "Your ovulation day is tomorrow — your most fertile moment. Whether you're trying or just tracking, stay aware."
                );
                case 0 -> new NotifCopy(
                        "Your fertile window is open ✨",
                        "Today is your ovulation day. Stay in tune with your body — this is your peak fertility moment."
                );
                default -> null;
            };

            case "FERTILITY" -> switch (days) {
                case 0 -> new NotifCopy(
                        "Your fertile window starts today 💕",
                        "You're entering your most fertile days. Track any symptoms and stay in tune with your body."
                );
                case 1 -> new NotifCopy(
                        "Fertile window starts tomorrow 💕",
                        "Your fertility window opens tomorrow. A great time to pay extra attention to how your body feels."
                );
                default -> null;
            };

            default -> {
                // Fallback for any other alert types
                if (alert.getMessage() != null) {
                    yield new NotifCopy(getTitle(alert.getType()), alert.getMessage());
                }
                yield null;
            }
        };
    }

    private String buildCaraInsightBody(User user) {
        // Placeholder — swap with real AI call from AiPredictionService
        return "Based on your logs this week, Cara has a new insight about your cycle. Tap to read it.";
    }

    private String getTitle(String type) {
        return switch (type) {
            case "PERIOD"    -> "Period Reminder 🩸";
            case "OVULATION" -> "Ovulation Alert 🌼";
            case "FERTILITY" -> "Fertility Window 💕";
            default          -> "Health Update 💙";
        };
    }

    private String resolveScreen(String type) {
        return switch (type) {
            case "PERIOD"    -> "Tracker";
            case "OVULATION" -> "FertilityTips";
            case "FERTILITY" -> "FertilityTips";
            default          -> "Home";
        };
    }

    // ─── Value record for notification copy ───────────────────────────────────
    private record NotifCopy(String title, String body) {}
}