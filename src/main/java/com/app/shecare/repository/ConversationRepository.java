package com.app.shecare.repository;

import com.app.shecare.entity.Conversation;
import com.app.shecare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // 🔥 Get all conversations of a user (latest first)
    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);
}