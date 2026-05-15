package com.app.shecare.config;

import io.github.bucket4j.Bucket;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        // ✅ Get IP safely
        String ip = req.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        }

        Bucket bucket = null;

        // =========================================================
        // AUTH APIs
        // =========================================================

        // LOGIN
        if (path.startsWith("/api/auth/login")) {

            bucket = rateLimitService.resolveBucket(
                    ip,
                    RateLimitType.LOGIN
            );
        }

        // REGISTER
        else if (path.startsWith("/api/auth/register")) {

            bucket = rateLimitService.resolveBucket(
                    ip,
                    RateLimitType.REGISTER
            );
        }

        // FORGOT PASSWORD
        else if (path.startsWith("/api/auth/forgot-password")) {

            bucket = rateLimitService.resolveBucket(
                    ip,
                    RateLimitType.FORGOT_PASSWORD
            );
        }

        // SEND OTP
        else if (path.startsWith("/api/auth/send-otp")) {

            bucket = rateLimitService.resolveBucket(
                    ip,
                    RateLimitType.SEND_OTP
            );
        }

        // VERIFY OTP
        else if (path.startsWith("/api/auth/verify-otp")) {

            bucket = rateLimitService.resolveBucket(
                    ip,
                    RateLimitType.VERIFY_OTP
            );
        }

        // =========================================================
        // CHAT API
        // =========================================================

        else if (path.startsWith("/api/chat")) {

            String userKey = getUserKey();

            bucket = rateLimitService.resolveBucket(
                    userKey,
                    RateLimitType.CHAT
            );
        }

        // =========================================================
        // VOICE STREAM API
        // =========================================================

        else if (path.startsWith("/api/voice/stream")) {

            String userKey = getUserKey();

            bucket = rateLimitService.resolveBucket(
                    userKey,
                    RateLimitType.VOICE_STREAM
            );
        }

        // =========================================================
        // COMMUNITY CREATE POST
        // =========================================================

        else if (
                path.startsWith("/api/community/posts")
                        && method.equals("POST")
        ) {

            String userKey = getUserKey();

            bucket = rateLimitService.resolveBucket(
                    userKey,
                    RateLimitType.CREATE_POST
            );
        }

        // =========================================================
        // COMMUNITY COMMENTS
        // =========================================================

        else if (
                path.contains("/comment")
                        && method.equals("POST")
        ) {

            String userKey = getUserKey();

            bucket = rateLimitService.resolveBucket(
                    userKey,
                    RateLimitType.COMMENT
            );
        }

        // =========================================================
        // PAYMENT LINK
        // =========================================================

        else if (path.startsWith("/api/subscription/payment-link")) {

            String userKey = getUserKey();

            bucket = rateLimitService.resolveBucket(
                    userKey,
                    RateLimitType.PAYMENT_LINK
            );
        }

        // =========================================================
        // RATE LIMIT CHECK
        // =========================================================

        if (bucket != null) {

            if (!bucket.tryConsume(1)) {

                res.setStatus(429);

                res.setContentType("application/json");

                res.getWriter().write("""
                    {
                      "error": "Too many requests. Please try again later."
                    }
                    """);

                return;
            }

            // Optional header
            res.addHeader(
                    "X-Rate-Limit-Remaining",
                    String.valueOf(bucket.getAvailableTokens())
            );
        }

        // Continue request
        chain.doFilter(request, response);
    }

    // =========================================================
    // GET AUTHENTICATED USER KEY
    // =========================================================

    private String getUserKey() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {

            return auth.getName();
        }

        return "anonymous";
    }
}