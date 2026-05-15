package com.app.shecare.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.shecare.dto.SosRequest;
import com.app.shecare.dto.SosResponse;
import com.app.shecare.entity.DeviceFcmToken;
import com.app.shecare.entity.EmergencyContact;
import com.app.shecare.entity.SosAlert;
import com.app.shecare.entity.SosAlertContact;
import com.app.shecare.entity.SosStatus;
import com.app.shecare.entity.User;
import com.app.shecare.exception.ResourceNotFoundException;
import com.app.shecare.exception.ValidationException;
import com.app.shecare.repository.DeviceFcmTokenRepository;
import com.app.shecare.repository.EmergencyContactRepository;
import com.app.shecare.repository.SosAlertContactRepository;
import com.app.shecare.repository.SosAlertRepository;
import com.app.shecare.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SosService {

    private final SosAlertRepository sosAlertRepo;
    private final SosAlertContactRepository sosAlertContactRepo;
    private final EmergencyContactRepository contactRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;  // your existing FCM service
    private final EmailService emailService;                // your existing SendGrid service
    private final DeviceFcmTokenRepository fcmTokenRepo;   // your existing token repo

    @Transactional
public SosResponse triggerSOS(Long userId, SosRequest request) {

    log.info("🚨 SOS triggered request received for userId: {}", userId);

    // 1. Get user
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    log.info("👤 User found: {}", user.getUsername());

    // 2. Get emergency contacts
    List<EmergencyContact> contacts = contactRepo
            .findByUserIdAndIsActiveTrue(user.getId());

    log.info("📞 Total emergency contacts found: {}", contacts.size());

    if (contacts.isEmpty()) {
        log.warn("⚠️ No emergency contacts found for user {}", userId);
        throw new ValidationException(
            "No emergency contacts found. Please add at least one."
        );
    }

    // 3. Save SOS alert
    SosAlert alert = SosAlert.builder()
            .user(user)
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .batteryLevel(request.getBatteryLevel())
            .voiceMessage(request.getVoiceMessage())
            .status(SosStatus.TRIGGERED)
            .build();

    SosAlert savedAlert = sosAlertRepo.save(alert);

    log.info("💾 SOS alert saved with ID: {}", savedAlert.getId());

    // 4. Build shared values
    String mapsLink;

if (request.getLatitude() != null && request.getLongitude() != null) {
    mapsLink = String.format("https://maps.google.com/?q=%s,%s",
            request.getLatitude(), request.getLongitude());
} else {
    mapsLink = "⚠️ Location is turned off on the user's device";
}

    log.info("📍 Maps link: {}", mapsLink);

    // 5. Notify all contacts
    List<SosAlertContact> notifiedList = contacts.stream().map(contact -> {

        log.info("➡️ Processing contact: {}", contact.getName());
        log.info("   📧 Email: {}", contact.getEmail());
        log.info("   📱 LinkedUserId: {}", contact.getLinkedUserId());

        boolean emailSent = false;
        boolean fcmSent   = false;

        // ── Email ──────────────────────────────
        if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
            try {
                log.info("📧 Attempting email to {}", contact.getEmail());

                emailService.sendSosAlertEmail(
                    contact.getEmail(),
                    contact.getName(),
                    user.getUsername(),
                    request.getVoiceMessage(),
                    mapsLink,
                    request.getBatteryLevel(),
                    request.getTimestamp()
                );

                emailSent = true;
                log.info("✅ Email triggered successfully for {}", contact.getEmail());

            } catch (Exception e) {
                log.error("❌ Email failed for {}: {}", contact.getEmail(), e.getMessage());
            }
        } else {
            log.warn("⚠️ No email found for contact {}", contact.getName());
        }

        // ── FCM Push ───────────────────────────
        if (contact.getLinkedUserId() != null) {

            List<DeviceFcmToken> tokens = fcmTokenRepo
                    .findByUserId(contact.getLinkedUserId());

            log.info("📲 FCM tokens found: {}", tokens.size());

            if (!tokens.isEmpty()) {
                tokens.forEach(t -> {
                    log.info("📲 Sending push to token: {}", t.getFcmToken());

                    notificationService.sendSosNotification(
                        t.getFcmToken(),
                        user.getUsername(),
                        mapsLink
                    );
                });

                fcmSent = true;
                log.info("✅ Push notification sent");
            } else {
                log.warn("⚠️ No FCM tokens for userId {}", contact.getLinkedUserId());
            }
        } else {
            log.warn("⚠️ Contact not linked to app user");
        }

        return SosAlertContact.builder()
                .sosAlert(savedAlert)
                .contact(contact)
                .emailSent(emailSent)
                .smsSent(false)
                .notifiedAt(LocalDateTime.now())
                .build();

    }).collect(Collectors.toList());

    sosAlertContactRepo.saveAll(notifiedList);

    // 6. Update status
    savedAlert.setStatus(SosStatus.NOTIFIED);
    sosAlertRepo.save(savedAlert);

    log.warn("🚨 SOS completed — user: {} — {} contacts processed",
            user.getUsername(), contacts.size());

    return SosResponse.builder()
            .alertId(savedAlert.getId())
            .status("NOTIFIED")
            .contactsNotified(contacts.size())
            .triggeredAt(savedAlert.getTriggeredAt())
            .build();
}

    public List<SosAlert> getSosHistory(Long userId) {
        return sosAlertRepo.findByUserIdOrderByTriggeredAtDesc(userId);
    }
}