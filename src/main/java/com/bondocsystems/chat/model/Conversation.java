package com.bondocsystems.chat.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue
    private UUID id;

    private Instant createdAt;
    private Instant updatedAt;

    // Constructors, getters, setters
}