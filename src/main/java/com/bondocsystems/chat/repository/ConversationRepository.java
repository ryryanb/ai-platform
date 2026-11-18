package com.bondocsystems.chat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bondocsystems.chat.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findAllByCustomerIdOrderByUpdatedAtDesc(String customerId);

    Optional<Conversation> findByIdAndCustomerId(UUID id, String customerId);
}