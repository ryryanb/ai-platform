package com.bondocsystems.chat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bondocsystems.chat.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}