package com.bondocsystems.chat.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
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

    private String title;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    private Instant createdAt;
    private Instant updatedAt;
}