package com.app.shecare.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // =========================================================
    // CACHE
    // =========================================================

    private final Map<String, Bucket> cache =
            new ConcurrentHashMap<>();

    // =========================================================
    // RESOLVE BUCKET
    // =========================================================

    public Bucket resolveBucket(
            String key,
            RateLimitType type
    ) {

        String finalKey =
                key + ":" + type.name();

        return cache.computeIfAbsent(
                finalKey,
                k -> createBucket(type)
        );
    }

    // =========================================================
    // CREATE BUCKETS
    // =========================================================

    private Bucket createBucket(
            RateLimitType type
    ) {

        return switch (type) {

            // =================================================
            // LOGIN
            // 5 requests / minute
            // =================================================

            case LOGIN -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    5,
                                    Refill.greedy(
                                            5,
                                            Duration.ofMinutes(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // REGISTER
            // 3 requests / hour
            // =================================================

            case REGISTER -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    3,
                                    Refill.greedy(
                                            3,
                                            Duration.ofHours(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // SEND OTP
            // 3 requests / 5 mins
            // =================================================

            case SEND_OTP -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    3,
                                    Refill.greedy(
                                            3,
                                            Duration.ofMinutes(5)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // VERIFY OTP
            // 5 requests / 5 mins
            // =================================================

            case VERIFY_OTP -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    5,
                                    Refill.greedy(
                                            5,
                                            Duration.ofMinutes(5)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // FORGOT PASSWORD
            // 3 requests / hour
            // =================================================

            case FORGOT_PASSWORD -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    3,
                                    Refill.greedy(
                                            3,
                                            Duration.ofHours(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // CHAT API
            // 15 requests / minute
            // =================================================

            case CHAT -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    15,
                                    Refill.greedy(
                                            15,
                                            Duration.ofMinutes(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // VOICE STREAM
            // 10 requests / hour
            // =================================================

            case VOICE_STREAM -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    10,
                                    Refill.greedy(
                                            10,
                                            Duration.ofHours(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // COMMUNITY POSTS
            // 10 requests / hour
            // =================================================

            case CREATE_POST -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    10,
                                    Refill.greedy(
                                            10,
                                            Duration.ofHours(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // COMMENTS
            // 30 requests / hour
            // =================================================

            case COMMENT -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    30,
                                    Refill.greedy(
                                            30,
                                            Duration.ofHours(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // PAYMENT LINK
            // 5 requests / hour
            // =================================================

            case PAYMENT_LINK -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    5,
                                    Refill.greedy(
                                            5,
                                            Duration.ofHours(1)
                                    )
                            )
                    )
                    .build();

            // =================================================
            // GLOBAL LIMIT
            // 300 requests / minute
            // =================================================

            case GLOBAL -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    300,
                                    Refill.greedy(
                                            300,
                                            Duration.ofMinutes(1)
                                    )
                            )
                    )
                    .build();
        };
    }
}