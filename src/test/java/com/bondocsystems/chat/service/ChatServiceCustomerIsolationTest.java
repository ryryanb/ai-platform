package com.bondocsystems.chat.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.AccessDeniedException;

import com.bondocsystems.chat.exception.ResourceNotFoundException;
import com.bondocsystems.chat.repository.ConversationRepository;
import com.bondocsystems.chat.repository.MessageRepository;

class ChatServiceCustomerIsolationTest {
    private ConversationRepository conversations;
    private MessageRepository messages;
    private ChatClient chatClient;
    private ChatService service;

    @BeforeEach
    void setUp() {
        conversations = mock(ConversationRepository.class);
        messages = mock(MessageRepository.class);
        chatClient = mock(ChatClient.class);
        service = new ChatService(chatClient, conversations, messages, mock(PromptEngineeringService.class));
    }

    @Test
    void rejectsConversationWhenItBelongsToAnotherCustomer() {
        UUID conversationId = UUID.randomUUID();
        when(conversations.findByIdAndCustomerId(conversationId, "customer-a")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getConversationHistory(conversationId, "customer-a"));

        verify(conversations).findByIdAndCustomerId(conversationId, "customer-a");
        verifyNoInteractions(messages, chatClient);
    }

    @Test
    void refusesBlankCustomerIdentity() {
        assertThrows(AccessDeniedException.class, () -> service.getConversations(" "));
        verify(conversations, never()).findAllByCustomerIdOrderByUpdatedAtDesc(" ");
    }
}