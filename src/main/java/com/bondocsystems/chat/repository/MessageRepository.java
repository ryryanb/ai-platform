package com.bondocsystems.chat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.bondocsystems.chat.model.Message;
import com.bondocsystems.chat.model.MessageRole;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdAndRole(
        @Param("conversationId") UUID conversationId,
        @Param("role") MessageRole role
    );
    
    List<Message> findByConversationId(
        @Param("conversationId") UUID conversationId
    );
    
    List<Message> findByConversationIdOrderByCreatedAt(
        @Param("conversationId") UUID conversationId
    );

    
}
