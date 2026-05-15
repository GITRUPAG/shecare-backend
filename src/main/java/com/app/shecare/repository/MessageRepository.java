package com.app.shecare.repository;

import com.app.shecare.entity.Message;
import com.app.shecare.entity.Conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface MessageRepository extends JpaRepository<Message, Long> {

    // Chat screen (ordered)
    Page<Message> findByConversationOrderByCreatedAtAsc(
            Conversation conversation,
            Pageable pageable
    );

    // Latest messages (optional)
    Page<Message> findByConversationOrderByCreatedAtDesc(
            Conversation conversation,
            Pageable pageable
    );

    // Last message (preview)
    Message findTopByConversationOrderByCreatedAtDesc(Conversation conversation);
}