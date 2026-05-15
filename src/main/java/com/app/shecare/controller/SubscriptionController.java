package com.app.shecare.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shecare.config.RazorpayConfig;
import com.app.shecare.entity.User;
import com.app.shecare.repository.PaymentLinkRepository;
import com.app.shecare.repository.UserRepository;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.SubscriptionService;
import com.razorpay.Utils;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private RazorpayConfig razorpayConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentLinkRepository paymentLinkRepository;

    // ✅ Create Order (keep for future native flow)
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder() throws Exception {

        String order = subscriptionService.createOrder(199);

        return ResponseEntity.ok(order);
    }

    // ✅ Verify (used in native SDK flow)
    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> data) {

        boolean isValid = subscriptionService.verifyPayment(
                data.get("orderId"),
                data.get("paymentId"),
                data.get("signature")
        );

        if (!isValid) {
            return ResponseEntity.badRequest().body("Payment verification failed");
        }

        subscriptionService.activatePremium(userDetails.getUser(), 30);

        return ResponseEntity.ok(Map.of(
                "message", "Payment successful, premium activated"
        ));
    }

    // ✅ NEW: Create Payment Link (Expo)
    @PostMapping("/payment-link")
    public ResponseEntity<?> createPaymentLink(
            @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

        String url = subscriptionService.createPaymentLink(userDetails.getUser());

        return ResponseEntity.ok(url);
    }

    // ✅ NEW: Webhook (MOST IMPORTANT)
    @PostMapping("/webhook")
public ResponseEntity<?> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Signature")
        String signature) {

    try {

        System.out.println("Webhook received");

        boolean isValid =
                Utils.verifyWebhookSignature(
                        payload,
                        signature,
                        razorpayConfig.getWebhookSecret()
                );

        if (!isValid) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid signature");
        }

        JSONObject event = new JSONObject(payload);

        String eventType = event.getString("event");

        System.out.println("Event Type: " + eventType);

        // =====================================================
        // PAYMENT SUCCESS
        // =====================================================

        if ("payment_link.paid".equals(eventType)) {

            JSONObject paymentLink = event
                    .getJSONObject("payload")
                    .getJSONObject("payment_link")
                    .getJSONObject("entity");

            JSONObject payment = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String referenceId =
                    paymentLink.getString("reference_id");

            String paymentId =
                    payment.getString("id");

            String method =
                    payment.optString("method");

            Integer amount =
                    payment.getInt("amount") / 100;

            Long userId = Long.parseLong(
                    referenceId.split("_")[1]
            );

            User user = userRepository
                    .findById(userId)
                    .orElseThrow();

            // activate premium

            subscriptionService
                    .activatePremium(user, 30);

            // update payment record

            paymentLinkRepository
                    .findByReferenceId(referenceId)
                    .ifPresent(record -> {

                        record.setStatus("PAID");

                        record.setRazorpayPaymentId(
                                paymentId
                        );

                        record.setPaymentMethod(
                                method
                        );

                        record.setAmount(amount);

                        record.setPaidAt(
                                LocalDateTime.now()
                        );

                        paymentLinkRepository
                                .save(record);
                    });

            System.out.println(
                    "Payment success for user: "
                            + userId
            );
        }

        return ResponseEntity.ok("ok");

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity
                .status(500)
                .body("Webhook error");
    }
}
    // ✅ Status
    @GetMapping("/status")
    public ResponseEntity<?> status(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();

        boolean isPrem = subscriptionService.isPremiumActive(user);
        int used = user.getChatCountToday() != null ? user.getChatCountToday() : 0;

        int remaining = Math.max(0, subscriptionService.getRemainingChats(user));

        Map<String, Object> resp = new HashMap<>();
        resp.put("isPremium", isPrem);
        resp.put("expiry", user.getPremiumExpiry());
        resp.put("free_used", used);
        resp.put("free_remaining", isPrem ? null : remaining);
        resp.put("free_limit", 20);

        return ResponseEntity.ok(resp);
    }

    // ✅ Deactivate
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivate(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        user.setIsPremium(false);
        user.setPremiumExpiry(null);

        userRepository.save(user); // 🔥 FIX

        return ResponseEntity.ok(Map.of("message", "Premium removed"));
    }
}