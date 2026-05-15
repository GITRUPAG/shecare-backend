package com.app.shecare.controller;

import com.app.shecare.entity.NotificationEntity;
import com.app.shecare.entity.User;
import com.app.shecare.repository.NotificationRepository;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ✅ 1. REGISTER FCM TOKEN
    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {

        Long userId = userDetails.getUser().getId();
        String token = body.get("fcmToken");

        notificationService.registerToken(userId, token);

        return ResponseEntity.ok().build();
    }

    // ✅ 2. GET ALL NOTIFICATIONS
    @GetMapping
    public List<NotificationEntity> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ✅ 3. UNREAD COUNT
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        long count = notificationRepository
                .countByUserIdAndIsReadFalse(userId);

        return Map.of("count", count);
    }

    // ✅ 4. MARK SINGLE AS READ
    @PutMapping("/{id}/read")
    public Map<String, String> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });

        return Map.of("status", "ok");
    }

    // ✅ 5. MARK ALL AS READ
    @PutMapping("/read-all")
    public Map<String, String> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        List<NotificationEntity> unread = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> !n.isRead())
                .toList();

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);

        return Map.of("status", "ok");
    }

    @GetMapping("/test")
public ResponseEntity<String> testNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long userId = userDetails.getUser().getId();

    notificationService.sendToUser(
            userId,
            "Test Notification 🚀",
            "If you see this, your FCM is working perfectly!",
            "TEST",
            "Home"
    );

    return ResponseEntity.ok("Test notification sent!");
}
}