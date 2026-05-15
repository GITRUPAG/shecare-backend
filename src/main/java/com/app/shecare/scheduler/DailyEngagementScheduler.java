package com.app.shecare.scheduler;

import com.app.shecare.dto.CycleCalendarResponse;
import com.app.shecare.entity.User;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.service.NotificationService;
import com.app.shecare.service.PeriodService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  DailyEngagementScheduler  —  SheCare user retention notifications
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  All notifications use NotificationService.sendToUser() which:
 *    ✅ Saves to DB          → visible in the in-app notification center
 *    ✅ Sends FCM push       → fires on CLOSED / MINIMIZED apps via system tray
 *    ✅ Respects quiet hours → no push between 10 PM – 8 AM (except SOS)
 *
 *  Schedule overview (IST, adjust if needed):
 *  ┌──────────┬───────────────────────────────────────────────────────────────┐
 *  │  7:00 AM │  Good Morning                                                 │
 *  │  8:00 AM │  Tip of the Day  (phase-aware)                                │
 *  │  9:00 AM │  Breakfast Reminder                                           │
 *  │ 11:00 AM │  Water Intake #1                                              │
 *  │  1:00 PM │  Lunch Reminder                                               │
 *  │  3:00 PM │  Water Intake #2                                              │
 *  │  5:00 PM │  Water Intake #3                                              │
 *  │  7:30 PM │  Dinner Reminder                                              │
 *  │  9:00 PM │  Good Night                                                   │
 *  └──────────┴───────────────────────────────────────────────────────────────┘
 *
 *  Emergency / SOS alerts are NOT scheduled — they are triggered in real-time
 *  via NotificationService.sendSosNotification() from SosService. They bypass
 *  quiet hours automatically.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyEngagementScheduler {

    private final UserRepository       userRepository;
    private final PeriodService        periodService;
    private final NotificationService  notificationService;

    private final Random random = new Random();

    // ═══════════════════════════════════════════════════════════════════════════
    //  GOOD MORNING  —  7:00 AM daily
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 7 * * ?")
    public void sendGoodMorning() {
        log.info("☀️ Good morning notification job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            pick(MORNING_TITLES),
            pick(morningBodies(phase))
        ), "DailyGreeting", "Home");
        log.info("✅ Good morning job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TIP OF THE DAY  —  8:00 AM daily (phase-aware)
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendTipOfTheDay() {
        log.info("💡 Tip of the day job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            "Today's tip for you 💡",
            phaseTip(phase)
        ), "CARA_INSIGHT", "CaraChat");
        log.info("✅ Tip of the day job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BREAKFAST  —  9:00 AM daily
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendBreakfastReminder() {
        log.info("🍳 Breakfast reminder job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            pick(BREAKFAST_TITLES),
            pick(breakfastBodies(phase))
        ), "MealReminder", "NutritionTips");
        log.info("✅ Breakfast job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WATER INTAKE #1  —  11:00 AM
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 11 * * ?")
    public void sendWaterReminder1() {
        log.info("💧 Water reminder #1 job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            pick(WATER_TITLES),
            pick(waterBodies(phase))
        ), "WaterReminder", "Home");
        log.info("✅ Water #1 job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LUNCH  —  1:00 PM daily
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 13 * * ?")
    public void sendLunchReminder() {
        log.info("🥗 Lunch reminder job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            pick(LUNCH_TITLES),
            pick(lunchBodies(phase))
        ), "MealReminder", "NutritionTips");
        log.info("✅ Lunch job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WATER INTAKE #2  —  3:00 PM
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 15 * * ?")
    public void sendWaterReminder2() {
        log.info("💧 Water reminder #2 job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            "Hydration check! 💧",
            "Afternoon slump? Skip the coffee — a big glass of water will perk you right up. You're doing amazing, keep it up! 🌸"
        ), "WaterReminder", "Home");
        log.info("✅ Water #2 job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WATER INTAKE #3  —  5:00 PM
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 17 * * ?")
    public void sendWaterReminder3() {
        log.info("💧 Water reminder #3 job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            "Last water reminder of the day 💦",
            "Have you had at least 8 glasses today? Your skin, energy, and hormones all thank you when you stay hydrated. 🌺"
        ), "WaterReminder", "Home");
        log.info("✅ Water #3 job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DINNER  —  7:30 PM daily
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 30 19 * * ?")
    public void sendDinnerReminder() {
        log.info("🍽️ Dinner reminder job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            pick(DINNER_TITLES),
            pick(dinnerBodies(phase))
        ), "MealReminder", "NutritionTips");
        log.info("✅ Dinner job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  GOOD NIGHT  —  9:00 PM daily
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 5 0 * * ?")
    public void sendGoodNight() {
        log.info("🌙 Good night notification job started");
        dispatchToAll((user, phase) -> new NotifCopy(
            pick(NIGHT_TITLES),
            pick(nightBodies(phase))
        ), "DailyGreeting", "Home");
        log.info("✅ Good night job done");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CORE DISPATCHER  —  resolves phase, builds copy, sends notification
    // ═══════════════════════════════════════════════════════════════════════════
    private void dispatchToAll(CopyBuilder builder, String type, String screen) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                String phase = resolvePhase(user);
                NotifCopy copy = builder.build(user, phase);
                if (copy == null) continue;

                notificationService.sendToUser(
                    user.getId(),
                    copy.title(),
                    copy.body(),
                    type,
                    screen
                );
            } catch (Exception e) {
                log.error("❌ Engagement notif failed for user {}: {}", user.getId(), e.getMessage());
            }
        }
    }

    /**
     * Resolves the user's current cycle phase via CycleCalendarResponse.
     * Returns "unknown" if no cycle data exists yet — copy should handle gracefully.
     */
    private String resolvePhase(User user) {
        try {
            CycleCalendarResponse calendar = periodService.getCycleCalendar(user);
            if (calendar == null || calendar.getCurrentPhase() == null) return "unknown";
            return calendar.getCurrentPhase(); // "Menstrual" | "Follicular" | "Ovulation" | "Fertile" | "Luteal" | "PMS"
        } catch (Exception e) {
            return "unknown";
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  COPY POOLS  — pick() returns a random element; add more strings freely
    // ═══════════════════════════════════════════════════════════════════════════

    // ── Morning titles ──────────────────────────────────────────────────────
    private static final String[] MORNING_TITLES = {
        "Good morning, gorgeous ☀️",
        "Rise and glow, queen 👑",
        "A new day, a new you 🌸",
        "Morning, superwoman! 💪",
        "Hey beautiful, it's a brand new day 🌺"
    };

    private String[] morningBodies(String phase) {
        String phaseNote = switch (phase) {
            case "Menstrual"  -> "Your body is doing incredible work right now. Be extra gentle with yourself today. 🩸";
            case "Follicular" -> "Your energy is rising — this is your season to shine! 🌱";
            case "Ovulation"  -> "You're literally glowing today — it's your peak phase! Use that energy. ✨";
            case "Fertile"    -> "Your body is in full bloom. Stay in tune with how you feel today. 🌸";
            case "Luteal"     -> "You might feel a bit more inward today, and that's perfectly okay. 💜";
            case "PMS"        -> "PMS is no joke. Be extra kind to yourself and reach out if you need anything. 💕";
            default           -> "Whatever this day holds, SheCare is right here with you. 💜";
        };
        return new String[]{
            phaseNote,
            "You woke up — that's already a win. Now go make something beautiful happen. 🌟 " + phaseNote,
            "Good morning! Hydrate, breathe, and take on the day. " + phaseNote
        };
    }

    // ── Breakfast titles ────────────────────────────────────────────────────
    private static final String[] BREAKFAST_TITLES = {
        "Breakfast time, babe 🍳",
        "Don't skip breakfast! 🥑",
        "Your body deserves fuel ☕",
        "Morning nourishment time 🌻",
        "Eat something yummy 💛"
    };

    private String[] breakfastBodies(String phase) {
        String tip = switch (phase) {
            case "Menstrual"  -> "Iron-rich foods like spinach and eggs are your best friends right now. Replenish what your body needs. 🥚";
            case "Follicular" -> "Energising foods like oats or smoothie bowls match your rising energy perfectly! 🌿";
            case "Ovulation"  -> "Antioxidant-rich breakfasts (berries, nuts, seeds) support your peak phase beautifully. 🫐";
            case "Fertile"    -> "Light, nourishing breakfasts keep your hormones happy today. 🍓";
            case "Luteal"     -> "Complex carbs like whole grain toast will help stabilise your mood and energy. 🍞";
            case "PMS"        -> "Magnesium-rich foods like banana and dark chocolate can ease those PMS symptoms. Yes, chocolate is allowed! 🍫";
            default           -> "A nourishing breakfast sets your whole day up. Please don't skip it, okay? 🥐";
        };
        return new String[]{ tip };
    }

    // ── Lunch titles ────────────────────────────────────────────────────────
    private static final String[] LUNCH_TITLES = {
        "Lunch o'clock! 🥗",
        "Midday fuel time 🌮",
        "Hey, step away from the screen 😄",
        "Lunch break — you've earned it 🍱",
        "Time to eat, queen 👑"
    };

    private String[] lunchBodies(String phase) {
        String tip = switch (phase) {
            case "Menstrual"  -> "Warm soups or dals are perfect today — comfort food that loves you back. 🍲";
            case "Follicular" -> "Try a vibrant salad with lean protein — your energy phase deserves fresh food! 🥗";
            case "Ovulation"  -> "Light and fresh is the vibe today. Your digestion is at its best — make the most of it! 🌿";
            case "Fertile"    -> "Zinc-rich foods like lentils or pumpkin seeds are great for you this week. 🫘";
            case "Luteal"     -> "A balanced lunch with good fats will keep your mood steady this afternoon. 🥑";
            case "PMS"        -> "Avoid too much salt and caffeine — they make bloating worse. A balanced warm meal is your friend! 🍛";
            default           -> "Whatever you're having, eat mindfully, sit down, and actually enjoy it. You deserve that break! 🌸";
        };
        return new String[]{
            tip,
            "Don't eat at your desk. Close the laptop. Take 20 minutes just for you. " + tip
        };
    }

    // ── Dinner titles ────────────────────────────────────────────────────────
    private static final String[] DINNER_TITLES = {
        "Dinner time! 🍽️",
        "Evening nourishment 🌙",
        "Wind down with a good meal 🫶",
        "End the day well, lovely 🌅",
        "Almost done — eat something warm 🍜"
    };

    private String[] dinnerBodies(String phase) {
        String tip = switch (phase) {
            case "Menstrual"  -> "Something warm and comforting tonight — your body is working hard and deserves it. 🍲";
            case "Follicular" -> "Keep dinner light and protein-rich to carry that upward energy into tomorrow. 🥩";
            case "Ovulation"  -> "Omega-3 rich foods like fish or flaxseeds are wonderful tonight. 🐟";
            case "Fertile"    -> "Gut-friendly foods like curd or lightly cooked veggies keep your system balanced. 🥦";
            case "Luteal"     -> "Tryptophan-rich foods like paneer, tofu, or turkey will help you sleep better tonight. 🛌";
            case "PMS"        -> "Warm herbal tea with dinner can reduce cramps and help you unwind. You've got this! 🫖";
            default           -> "Keep dinner light and eat at least 2 hours before bed — your body will thank you in the morning! 🌙";
        };
        return new String[]{ tip };
    }

    // ── Water titles ────────────────────────────────────────────────────────
    private static final String[] WATER_TITLES = {
        "Hydration check 💧",
        "Water o'clock! 💦",
        "Drink up, gorgeous 🌊",
        "Reminder from SheCare 💜",
        "Your hormones love water 💧"
    };

    private String[] waterBodies(String phase) {
        String tip = switch (phase) {
            case "Menstrual"  -> "Staying hydrated can actually reduce menstrual cramps. Drink that water, queen! 💧";
            case "Follicular" -> "Good hydration helps your follicles develop beautifully. Fill up that glass! 🌿";
            case "Ovulation"  -> "Cervical mucus is mostly water — staying hydrated supports your body right now. 💦";
            case "Fertile"    -> "Extra hydration this week supports your fertile window. Drink up! 💜";
            case "Luteal"     -> "Water helps reduce bloating in your luteal phase. Counter-intuitive but true! 💧";
            case "PMS"        -> "Hydration reduces PMS headaches and bloating. A glass of water is basically medicine right now. 💊";
            default           -> "Aim for 8 glasses today. Your skin, hormones, and mood are all counting on it! 🌸";
        };
        return new String[]{ tip };
    }

    // ── Night titles ────────────────────────────────────────────────────────
    private static final String[] NIGHT_TITLES = {
        "Good night, lovely 🌙",
        "Rest well, you deserve it 💜",
        "Sweet dreams, superstar ✨",
        "Time to recharge, queen 👑",
        "Goodnight from SheCare 🌸"
    };

    private String[] nightBodies(String phase) {
        String tip = switch (phase) {
            case "Menstrual"  -> "You navigated today while on your period. That alone makes you a warrior. Sleep deeply. 🩸💜";
            case "Follicular" -> "Great day in your follicular phase! Rest well so tomorrow's energy is even better. 🌱";
            case "Ovulation"  -> "Peak phase, peak you! Rest well tonight so that glow carries into tomorrow. ✨";
            case "Fertile"    -> "Your body has been doing beautiful work today. Give it the rest it deserves tonight. 🌸";
            case "Luteal"     -> "Be proud of how you showed up today, even if it felt hard. Luteal phase is no joke. 💪";
            case "PMS"        -> "PMS is temporary. Tomorrow will be a little lighter. Sleep, and let your body heal. 💕";
            default           -> "Log off, put the phone down, and get 7-8 hours. SheCare will be here when you wake up. 💜";
        };
        return new String[]{
            tip,
            "Before you sleep — drink a glass of water, take a deep breath, and remind yourself: you did great today. " + tip
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PHASE-AWARE TIP OF THE DAY
    // ═══════════════════════════════════════════════════════════════════════════
    private String phaseTip(String phase) {
        return switch (phase) {
            case "Menstrual" -> pick(new String[]{
                "💡 Heat therapy (heating pad or warm water bag) can reduce cramps by up to 50%. Try it tonight!",
                "💡 Avoid caffeine today — it constricts blood vessels and can worsen cramps. Herbal tea is your bestie.",
                "💡 Gentle yoga poses like child's pose and cat-cow are made for period days. Try 10 mins!",
                "💡 Your pain tolerance is higher during menstruation — but that doesn't mean push through. Rest is productive.",
                "💡 Dark chocolate (70%+) contains magnesium which helps with cramps AND mood. You're welcome. 🍫"
            });
            case "Follicular" -> pick(new String[]{
                "💡 This is your best phase to try new workouts, start projects, or have difficult conversations. Your brain is sharp!",
                "💡 Estrogen is rising — you're naturally more social and energetic. Lean into it!",
                "💡 Great time to meal prep and plan your week. Your focus is at its peak right now.",
                "💡 Your skin is at its clearest in the follicular phase. A great time to go makeup-free! ✨",
                "💡 Start that thing you've been putting off. Seriously — your follicular energy is gold. Use it."
            });
            case "Ovulation" -> pick(new String[]{
                "💡 You're at your most charismatic and confident today. Schedule that presentation or important call!",
                "💡 Ovulation can cause a slight one-sided twinge (Mittelschmerz). Totally normal — just your body signalling!",
                "💡 Your voice is literally higher-pitched during ovulation (science!). Great day to record that voice note or video.",
                "💡 Your pain tolerance is highest now — great day for a wax, dental visit, or tough workout. 💪",
                "💡 If you're tracking fertility: cervical mucus resembles egg whites at ovulation. This is your peak fertile window."
            });
            case "Fertile" -> pick(new String[]{
                "💡 Fertile window = the 5-6 days around ovulation. Track any changes in your body carefully this week.",
                "💡 Your libido naturally peaks during your fertile window — completely normal and healthy! 🌸",
                "💡 Stress can delay ovulation even mid-cycle. A 5-minute breathing exercise today can genuinely help.",
                "💡 Zinc-rich foods (pumpkin seeds, chickpeas) support egg health. Add them to your meals this week!",
                "💡 Your immune system is slightly suppressed during the fertile window — extra hand-washing goes a long way."
            });
            case "Luteal" -> pick(new String[]{
                "💡 Luteal phase = progesterone peaks. You may feel more introverted — that's biology, not weakness.",
                "💡 Blood sugar fluctuations are common now. Small frequent meals beat 3 large ones this week.",
                "💡 Magnesium (nuts, seeds, leafy greens) is clinically shown to reduce luteal mood swings. 🥬",
                "💡 Your metabolism is slightly faster in the luteal phase — your body needs a bit more fuel than usual.",
                "💡 Journaling in the luteal phase can be incredibly therapeutic. Your emotions are vivid and real right now."
            });
            case "PMS" -> pick(new String[]{
                "💡 PMS symptoms peak 1-2 days before your period. You're almost through it — hang in there! 💪",
                "💡 Reducing salt intake in the last 5 days can significantly reduce bloating and breast tenderness.",
                "💡 Exercise releases endorphins that directly counteract PMS mood symptoms — even a 20-min walk helps.",
                "💡 Calcium (dairy, almonds, ragi) is proven to reduce PMS severity. Add it to your daily meals!",
                "💡 Feeling overwhelmed by emotions right now? Totally valid — progesterone is dropping rapidly. Be extra gentle with yourself."
            });
            default -> pick(new String[]{
                "💡 Log your period dates to unlock personalised phase insights, tips, and predictions just for you!",
                "💡 Tracking your cycle for just 3 months gives SheCare enough data to predict your next 6 cycles accurately.",
                "💡 Did you know stress is the #1 cause of irregular cycles? A 5-minute meditation a day can genuinely help. 🧘",
                "💡 Sleep and hormones are deeply connected. Even one night of poor sleep can affect your cycle. Rest is self-care!",
                "💡 SheCare tip: log your mood daily to spot patterns tied to your cycle. Awareness is the first step to balance. 💜"
            });
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════
    private String pick(String[] arr) {
        return arr[random.nextInt(arr.length)];
    }

    @FunctionalInterface
    private interface CopyBuilder {
        NotifCopy build(User user, String phase);
    }

    private record NotifCopy(String title, String body) {}
}