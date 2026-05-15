package com.app.shecare.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.shecare.entity.User;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.CaraClientService;
import com.app.shecare.service.ChatService;
import com.app.shecare.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SubscriptionService subscriptionService;
    private final ChatService chatService;
    private final CaraClientService caraClientService;

    @PostMapping
    public ResponseEntity<?> chat(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails, 
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

        // ✅ 1. Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        String token = authHeader.substring(7);

        // ✅ 2. Validate message
        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message required");
        }

        // ✅ 3. Get user
        User user = userDetails.getUser();

        // ✅ 4. Check subscription
        subscriptionService.validateAndConsume(user);

        // ✅ 5. Get conversationId (optional)
        UUID conversationId = null;
        if (body.get("conversationId") != null) {
            conversationId = UUID.fromString(body.get("conversationId"));
        }

        try {
            // 🔥 MAIN LOGIC (NEW)
            Map<String, Object> response = chatService.chat(
                    user,
                    conversationId,
                    message,
                    token
            );

            // ✅ 6. Add remaining chats
            int remaining = Math.max(0, subscriptionService.getRemainingChats(user));
            response.put("remainingChats", remaining);

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Cara service error"
            );
        }
    }


   // 🔥 STREAMING API (MAIN CHAT)
    // @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public Flux<String> stream(
    //         @RequestParam String message,
    //         @RequestParam(required = false) UUID conversationId,
    //         @AuthenticationPrincipal CustomUserDetails userDetails,
    //         @RequestHeader("Authorization") String authHeader
    // ) {

    //     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    //         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
    //     }

    //     String token = authHeader.substring(7);
    //     User user = userDetails.getUser();

    //     // ✅ subscription check
    //     subscriptionService.validateAndConsume(user);

    //     return chatService.streamChat(user, conversationId, message, token);
    // }


}