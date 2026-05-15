package com.app.shecare.service;

import com.app.shecare.entity.*;
import com.app.shecare.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;

    // ✅ Create conversation
    public Conversation createConversation(User user) {
        Conversation c = Conversation.builder()
                .user(user)
                .title("New Chat")
                .build();

        return conversationRepo.save(c);
    }

    // ✅ Get all conversations (sidebar)
    public List<Conversation> getUserConversations(User user) {
        return conversationRepo.findByUserOrderByUpdatedAtDesc(user);
    }

    // ✅ Get messages with pagination
    public List<Message> getMessages(Conversation conversation, int page, int size) {
        return messageRepo
                .findByConversationOrderByCreatedAtAsc(
                        conversation,
                        PageRequest.of(page, size)
                )
                .getContent();
    }

    // ✅ Save message
    public Message saveMessage(Conversation conversation, String role, String content, String emotion) {
        Message m = Message.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .emotion(emotion)
                .build();

        return messageRepo.save(m);
    }

    // ✅ Update last message preview
    public void updateLastMessage(Conversation conversation, String content) {
        conversation.setLastMessage(content);
        conversationRepo.save(conversation);
    }

    // ✅ Get conversation by ID
    public Conversation getConversation(UUID id) {
        return conversationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    // ✅ Delete conversation (secure)
    public void deleteConversation(UUID conversationId, User user) {

        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 🔐 security check
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        conversationRepo.delete(conversation);
    }
}