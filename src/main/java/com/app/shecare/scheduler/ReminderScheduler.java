// package com.app.shecare.scheduler;

// import com.app.shecare.entity.User;
// import com.app.shecare.repository.UserRepository;
// import com.app.shecare.service.NotificationService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import java.time.LocalDate;
// import java.util.List;

// @Component
// @RequiredArgsConstructor
// public class ReminderScheduler {

//     private final UserRepository userRepository;
//     private final NotificationService notificationService;

//     // Runs every day at 9 AM
//     @Scheduled(cron = "0 0 9 * * ?")
//     public void sendPeriodReminders() {

//         List<User> users = userRepository.findAll();

//         for (User user : users) {
//             LocalDate nextPeriod = user.getNextPeriodStart(); // make sure you store this

//             if (nextPeriod == null) continue;

//             long daysLeft = LocalDate.now().until(nextPeriod).getDays();

//             if (daysLeft == 2) {
//                 notificationService.sendToUser(
//                         user.getId(),
//                         "Period Reminder 🩸",
//                         "Your period is expected in 2 days",
//                         "PERIOD_REMINDER",
//                         "Tracker"
//                 );
//             }

//             if (daysLeft == 0) {
//                 notificationService.sendToUser(
//                         user.getId(),
//                         "Period Today 🌸",
//                         "Your period may start today",
//                         "PERIOD_TODAY",
//                         "Tracker"
//                 );
//             }
//         }
//     }
// }