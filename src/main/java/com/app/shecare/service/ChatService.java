package com.app.shecare.service;

import com.app.shecare.entity.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationService conversationService;
    private final CaraClientService caraClientService;

    public Map<String, Object> chat(
            User user,
            UUID conversationId,
            String message,
            String token
    ) {

        // 🔥 1. SAFE CONVERSATION FETCH (CRITICAL FIX)
        Conversation conversation = null;

        if (conversationId != null) {
            try {
                conversation = conversationService.getConversation(conversationId);

                // 🔐 ensure same user
                if (!conversation.getUser().getId().equals(user.getId())) {
                    conversation = null;
                }

            } catch (Exception e) {
                conversation = null;
            }
        }

        // 🔥 create only if needed
        if (conversation == null) {
            conversation = conversationService.createConversation(user);
        }

        // 🔥 DEBUG (remove later)
        System.out.println("Incoming conversationId: " + conversationId);
        System.out.println("Using conversation: " + conversation.getId());

        // 🔥 Auto-title
        if (conversation.getTitle() == null || conversation.getTitle().equals("New Chat")) {
            conversation.setTitle(generateTitle(message));
        }

        Map<String, Object> result = new HashMap<>();

        try {
            // 🔥 Call AI FIRST
            var cara = caraClientService.sendMessage(token, message);

            // 🔥 Save messages
            conversationService.saveMessage(conversation, "user", message, null);

            conversationService.saveMessage(
                    conversation,
                    "assistant",
                    cara.reply(),
                    cara.emotion()
            );

            // 🔥 Update preview
            conversationService.updateLastMessage(conversation, cara.reply());

            result.put("reply", cara.reply());
            result.put("emotion", cara.emotion());
            result.put("conversationId", conversation.getId());

        } catch (Exception e) {
            result.put("reply", "I'm here for you 💗");
            result.put("emotion", "neutral");
            result.put("conversationId", conversation.getId());
        }

        return result;
    }

    private String generateTitle(String message) {
        if (message == null || message.isBlank()) return "New Chat";

        message = message.trim();

        return message.length() > 30
                ? message.substring(0, 30) + "..."
                : message;
    }

//     public Flux<String> streamChat(
//         User user,
//         UUID conversationId,
//         String message,
//         String token
// ) {

//     Conversation conversation = null;

//     if (conversationId != null) {
//         try {
//             conversation = conversationService.getConversation(conversationId);

//             if (!conversation.getUser().getId().equals(user.getId())) {
//                 conversation = null;
//             }

//         } catch (Exception ignored) {}
//     }

//     if (conversation == null) {
//         conversation = conversationService.createConversation(user);
//     }

//     if (conversation.getTitle() == null || conversation.getTitle().equals("New Chat")) {
//         conversation.setTitle(generateTitle(message));
//     }

//     // ✅ 🔥 FIX: create final reference
//     final Conversation finalConversation = conversation;

//     StringBuilder fullReply = new StringBuilder();

//     return caraClientService.streamMessage(token, message)
//             .map(chunk -> {

//                 if (chunk.contains("\"type\":\"chunk\"")) {
//                     String text = extractText(chunk);
//                     fullReply.append(text);
//                 }

//                 // ✅ Proper SSE format
//                 return "data: " + chunk + "\n\n";
//             })
//             .doOnComplete(() -> {

//                 // ✅ use finalConversation (FIXED)
//                 conversationService.saveMessage(finalConversation, "user", message, null);

//                 conversationService.saveMessage(
//                         finalConversation,
//                         "assistant",
//                         fullReply.toString(),
//                         "neutral"
//                 );

//                 conversationService.updateLastMessage(finalConversation, fullReply.toString());
//             });
// }

    // 🔥 helper to extract text from chunk
    private String extractText(String json) {
        try {
            int start = json.indexOf("\"text\":\"") + 8;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}