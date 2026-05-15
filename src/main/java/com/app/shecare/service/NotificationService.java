package com.app.shecare.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.shecare.entity.DeviceFcmToken;
import com.app.shecare.entity.HealthMetric;
import com.app.shecare.entity.NotificationEntity;
import com.app.shecare.repository.DeviceFcmTokenRepository;
import com.app.shecare.repository.NotificationRepository;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DeviceFcmTokenRepository tokenRepository;
    private final NotificationRepository notificationRepository;

    // ─────────────────────────────────────────────────────────────
    // CHANNEL IDS
    // ─────────────────────────────────────────────────────────────
    private static final String CHANNEL_DEFAULT = "shecare_default";
    private static final String CHANNEL_HEALTH = "shecare_health_alerts";
    private static final String CHANNEL_SOS = "shecare_sos";

    // ─────────────────────────────────────────────────────────────
    // QUIET HOURS
    // ─────────────────────────────────────────────────────────────
    private boolean isQuietHours() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(22, 0))
                || now.isBefore(LocalTime.of(8, 0));
    }

    // ─────────────────────────────────────────────────────────────
    // ANDROID CONFIG
    // ─────────────────────────────────────────────────────────────
    private AndroidConfig buildAndroidConfig(String channelId) {

        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)

                .setNotification(
                        AndroidNotification.builder()
                                .setChannelId(channelId)
                                .setSound("default")
                                .setPriority(AndroidNotification.Priority.HIGH)
                                .setVibrateTimingsInMillis(
                                        new long[]{0, 250, 250, 250}
                                )
                                .setDefaultVibrateTimings(false)
                                .build()
                )
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // REGISTER FCM TOKEN (FIXED)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void registerToken(Long userId, String fcmToken) {

        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("Attempted to register empty FCM token");
            return;
        }

        // Check if token already exists globally
        Optional<DeviceFcmToken> existingToken =
                tokenRepository.findByFcmToken(fcmToken);

        if (existingToken.isPresent()) {

            DeviceFcmToken tokenEntity = existingToken.get();

            // Same user already has this token
            if (tokenEntity.getUserId().equals(userId)) {

                log.info(
                        "FCM token already registered for user {}",
                        userId
                );

                return;
            }

            // Token belongs to another user -> reassign
            tokenEntity.setUserId(userId);

            tokenRepository.save(tokenEntity);

            log.info(
                    "Reassigned FCM token to user {}",
                    userId
            );

            return;
        }

        // Save brand-new token
        tokenRepository.save(
                DeviceFcmToken.builder()
                        .userId(userId)
                        .fcmToken(fcmToken)
                        .build()
        );

        log.info("Registered NEW FCM token for user {}", userId);
    }

    // ─────────────────────────────────────────────────────────────
    // HEALTH METRIC ALERTS
    // ─────────────────────────────────────────────────────────────
    public void checkAndNotify(Long userId, HealthMetric metric) {

        String alertMessage = buildAlertMessage(metric);

        if (alertMessage == null) return;

        List<DeviceFcmToken> tokens =
                tokenRepository.findByUserId(userId);

        if (tokens.isEmpty()) return;

        List<String> fcmTokens = tokens.stream()
                .map(DeviceFcmToken::getFcmToken)
                .toList();

        sendMulticast(
                "Health alert",
                alertMessage,
                fcmTokens,
                CHANNEL_HEALTH
        );
    }

    private String buildAlertMessage(HealthMetric m) {

        return switch (m.getType()) {

            case HEART_RATE -> {

                int bpm = m.getValue().intValue();

                if (bpm < 50)
                    yield "Low heart rate detected: " + bpm + " bpm";

                if (bpm > 120)
                    yield "High heart rate detected: " + bpm + " bpm";

                yield null;
            }

            case SPO2 -> {

                int pct = m.getValue().intValue();

                if (pct < 94)
                    yield "Low blood oxygen: " + pct + "%";

                yield null;
            }

            case BLOOD_PRESSURE -> {

                if (m.getValue() == null
                        || m.getValueSecondary() == null) {

                    yield null;
                }

                int sys = m.getValue().intValue();
                int dia = m.getValueSecondary().intValue();

                if (sys > 140 || dia > 90
                        || sys < 90 || dia < 60) {

                    yield "Unusual blood pressure: "
                            + sys + "/" + dia + " mmHg";
                }

                yield null;
            }

            default -> null;
        };
    }

    // ─────────────────────────────────────────────────────────────
    // SOS NOTIFICATION
    // ─────────────────────────────────────────────────────────────
    public void sendSosNotification(
            String fcmToken,
            String userName,
            String mapsLink
    ) {

        MulticastMessage message = MulticastMessage.builder()

                .setAndroidConfig(
                        buildAndroidConfig(CHANNEL_SOS)
                )

                .setNotification(
                        Notification.builder()
                                .setTitle(
                                        "🚨 SOS Alert — "
                                                + userName
                                                + " needs help!"
                                )
                                .setBody(
                                        "Tap to see her location immediately"
                                )
                                .build()
                )

                .putData("type", "SOS_ALERT")
                .putData("location", mapsLink)
                .putData("userName", userName)

                .addToken(fcmToken)

                .build();

        try {

            BatchResponse response =
                    FirebaseMessaging.getInstance()
                            .sendEachForMulticast(message);

            log.warn(
                    "🚨 SOS FCM sent — success: {}, failed: {}",
                    response.getSuccessCount(),
                    response.getFailureCount()
            );

        } catch (FirebaseMessagingException e) {

            log.error(
                    "SOS FCM failed: {}",
                    e.getMessage()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CARA AI INSIGHT
    // ─────────────────────────────────────────────────────────────
    public void sendCaraInsight(
            Long userId,
            String insightBody
    ) {

        sendToUser(
                userId,
                "Cara noticed something for you 💜",
                insightBody,
                "CARA_INSIGHT",
                "CaraChat",
                null
        );
    }

    // ─────────────────────────────────────────────────────────────
    // SEND TO USER
    // ─────────────────────────────────────────────────────────────
    public void sendToUser(
            Long userId,
            String title,
            String body,
            String type,
            String screen,
            String postId
    ) {

        // Save notification in DB
        notificationRepository.save(

                NotificationEntity.builder()
                        .userId(userId)
                        .title(title)
                        .message(body)
                        .type(type)
                        .screen(screen)
                        .postId(postId)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        boolean isSos = "SOS_ALERT".equals(type);

        // Quiet hours
        if (!isSos && isQuietHours()) {
            return;
        }

        List<String> tokens =
                tokenRepository.findByUserId(userId)
                        .stream()
                        .map(DeviceFcmToken::getFcmToken)
                        .toList();

        if (tokens.isEmpty()) {
            return;
        }

        String channelId =
                isSos ? CHANNEL_SOS : CHANNEL_DEFAULT;

        MulticastMessage.Builder builder =
                MulticastMessage.builder()

                        .setAndroidConfig(
                                buildAndroidConfig(channelId)
                        )

                        .setNotification(
                                Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build()
                        )

                        .putData("title", title)
                        .putData("body", body)
                        .putData("type", type)
                        .putData("screen", screen);

        if (postId != null) {
            builder.putData("postId", postId);
        }

        builder.addAllTokens(tokens);

        try {

            BatchResponse response =
                    FirebaseMessaging.getInstance()
                            .sendEachForMulticast(builder.build());

            log.info(
                    "FCM sent — success: {}, failed: {}",
                    response.getSuccessCount(),
                    response.getFailureCount()
            );

            // Cleanup invalid tokens
            List<SendResponse> responses =
                    response.getResponses();

            for (int i = 0; i < responses.size(); i++) {

                if (!responses.get(i).isSuccessful()) {

                    MessagingErrorCode code =
                            responses.get(i)
                                    .getException()
                                    .getMessagingErrorCode();

                    if (code == MessagingErrorCode.UNREGISTERED
                            || code == MessagingErrorCode.INVALID_ARGUMENT) {

                        tokenRepository.deleteByFcmToken(
                                tokens.get(i)
                        );

                        log.info(
                                "Removed stale FCM token"
                        );
                    }
                }
            }

        } catch (FirebaseMessagingException e) {

            log.error(
                    "FCM failed for user {}: {}",
                    userId,
                    e.getMessage()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // OVERLOAD
    // ─────────────────────────────────────────────────────────────
    public void sendToUser(
            Long userId,
            String title,
            String body,
            String type,
            String screen
    ) {

        sendToUser(
                userId,
                title,
                body,
                type,
                screen,
                null
        );
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL MULTICAST
    // ─────────────────────────────────────────────────────────────
    private void sendMulticast(
            String title,
            String body,
            List<String> tokens,
            String channelId
    ) {

        MulticastMessage message =
                MulticastMessage.builder()

                        .setAndroidConfig(
                                buildAndroidConfig(channelId)
                        )

                        .setNotification(
                                Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build()
                        )

                        .addAllTokens(tokens)

                        .build();

        try {

            BatchResponse response =
                    FirebaseMessaging.getInstance()
                            .sendEachForMulticast(message);

            log.info(
                    "FCM health alert: {} success, {} failure",
                    response.getSuccessCount(),
                    response.getFailureCount()
            );

            List<SendResponse> responses =
                    response.getResponses();

            for (int i = 0; i < responses.size(); i++) {

                if (!responses.get(i).isSuccessful()) {

                    MessagingErrorCode code =
                            responses.get(i)
                                    .getException()
                                    .getMessagingErrorCode();

                    if (code == MessagingErrorCode.UNREGISTERED
                            || code == MessagingErrorCode.INVALID_ARGUMENT) {

                        tokenRepository.deleteByFcmToken(
                                tokens.get(i)
                        );
                    }
                }
            }

        } catch (FirebaseMessagingException e) {

            log.error(
                    "FCM multicast failed: {}",
                    e.getMessage()
            );
        }
    }
}