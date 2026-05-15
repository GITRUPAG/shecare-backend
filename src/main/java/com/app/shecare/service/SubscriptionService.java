package com.app.shecare.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.app.shecare.config.RazorpayConfig;
import com.app.shecare.entity.PaymentLinkRecord;
import com.app.shecare.entity.User;
import com.app.shecare.repository.PaymentLinkRepository;
import com.app.shecare.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class SubscriptionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RazorpayConfig razorpayConfig;

    @Autowired
    private PaymentLinkRepository paymentLinkRepository;

    private static final int FREE_LIMIT = 20;

    // =========================================================
    // PREMIUM STATUS
    // =========================================================

    public boolean isPremiumActive(User user) {

        if (Boolean.TRUE.equals(user.getIsPremium())
                && user.getPremiumExpiry() != null) {

            if (user.getPremiumExpiry()
                    .isAfter(LocalDateTime.now())) {

                return true;
            }

            // expired
            user.setIsPremium(false);
            user.setPremiumExpiry(null);

            userRepository.save(user);
        }

        return false;
    }

    // =========================================================
    // ACTIVATE PREMIUM
    // =========================================================

    public void activatePremium(User user, int days) {

        LocalDateTime base =
                user.getPremiumExpiry() != null
                        && user.getPremiumExpiry()
                        .isAfter(LocalDateTime.now())
                        ? user.getPremiumExpiry()
                        : LocalDateTime.now();

        user.setIsPremium(true);

        user.setPremiumExpiry(base.plusDays(days));

        userRepository.save(user);
    }

    // =========================================================
    // CREATE ORDER
    // =========================================================

    public String createOrder(int amount) throws Exception {

        RazorpayClient client = razorpayConfig.getClient();

        JSONObject options = new JSONObject();

        options.put("amount", amount * 100);

        options.put("currency", "INR");

        options.put("receipt",
                "txn_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        return order.toString();
    }

    // =========================================================
    // VERIFY PAYMENT
    // =========================================================

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature
    ) {

        try {

            return Utils.verifySignature(
                    orderId + "|" + paymentId,
                    signature,
                    razorpayConfig.getKeySecret()
            );

        } catch (Exception e) {

            return false;
        }
    }

    // =========================================================
    // FREE CHAT LIMIT
    // =========================================================

    public void validateAndConsume(User user) {

        LocalDate today = LocalDate.now();

        if (user.getLastChatDate() == null
                || !user.getLastChatDate().equals(today)) {

            user.setChatCountToday(0);

            user.setLastChatDate(today);
        }

        if (!isPremiumActive(user)
                && user.getChatCountToday() >= FREE_LIMIT) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "FREE_LIMIT_EXCEEDED"
            );
        }

        user.setChatCountToday(
                user.getChatCountToday() + 1
        );

        userRepository.save(user);
    }

    public int getRemainingChats(User user) {

        if (isPremiumActive(user)) {
            return Integer.MAX_VALUE;
        }

        int used = user.getChatCountToday() != null
                ? user.getChatCountToday()
                : 0;

        return Math.max(0, FREE_LIMIT - used);
    }

    // =========================================================
    // CREATE PAYMENT LINK
    // =========================================================

    public String createPaymentLink(User user)
            throws Exception {

        Optional<PaymentLinkRecord> existing =
                paymentLinkRepository
                        .findTopByUserIdAndStatusOrderByCreatedAtDesc(
                                user.getId(),
                                "CREATED"
                        );

        if (existing.isPresent()) {

            return existing.get().getShortUrl();
        }

        RazorpayClient client =
                razorpayConfig.getClient();

        String referenceId =
                "user_"
                        + user.getId()
                        + "_"
                        + System.currentTimeMillis();

        JSONObject request = new JSONObject();

        request.put("amount", 9900);

        request.put("currency", "INR");

        request.put(
                "description",
                "SheCare Premium — 1 month"
        );

        request.put("reference_id", referenceId);

        JSONObject customer = new JSONObject();

        customer.put("name", user.getUsername());

        customer.put("email", user.getEmail());

        if (user.getPhoneNumber() != null) {

            customer.put(
                    "contact",
                    user.getPhoneNumber()
            );
        }

        request.put("customer", customer);

        request.put(
                "callback_url",
                "shecare://payment/callback"
        );

        request.put(
                "callback_method",
                "get"
        );

        com.razorpay.PaymentLink link =
                client.paymentLink.create(request);

        String shortUrl =
                link.get("short_url").toString();

        String paymentLinkId =
                link.get("id").toString();

        // SAVE DB

        PaymentLinkRecord record =
                new PaymentLinkRecord();

        record.setUserId(user.getId());

        record.setReferenceId(referenceId);

        record.setShortUrl(shortUrl);

        record.setStatus("CREATED");

        record.setCreatedAt(LocalDateTime.now());

        record.setAmount(99);

        record.setRazorpayPaymentLinkId(
                paymentLinkId
        );

        paymentLinkRepository.save(record);

        return shortUrl;
    }
}