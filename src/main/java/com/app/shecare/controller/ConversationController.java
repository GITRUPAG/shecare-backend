package com.app.shecare.controller;

import com.app.shecare.entity.*;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // ✅ Get all conversations (sidebar)
    @GetMapping
    public ResponseEntity<List<Conversation>> getConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(
                conversationService.getUserConversations(user)
        );
    }

    // ✅ Get messages of a conversation
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Conversation conversation = conversationService.getConversation(id);

        return ResponseEntity.ok(
                conversationService.getMessages(conversation, page, size)
        );
    }

    // ✅ Delete conversation
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConversation(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        conversationService.deleteConversation(id, userDetails.getUser());
        return ResponseEntity.ok("Conversation deleted");
    }
}