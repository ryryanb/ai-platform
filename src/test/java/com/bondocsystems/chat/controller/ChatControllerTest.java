package com.bondocsystems.chat.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bondocsystems.chat.exception.ResourceNotFoundException;
import com.bondocsystems.chat.model.Conversation;
import com.bondocsystems.chat.model.Message;
import com.bondocsystems.chat.model.MessageRole;
import com.bondocsystems.chat.service.ChatService;

import reactor.core.publisher.Flux;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
private ChatService chatService;

    @Test
    void createConversation_shouldReturnCreated() throws Exception {
        UUID id = UUID.randomUUID();

        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());

        when(chatService.createConversation())
                .thenReturn(conversation);

        mockMvc.perform(post("/api/conversations"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getConversations_shouldReturnConversations()
            throws Exception {

        Conversation conversation = new Conversation();

        UUID id = UUID.randomUUID();

        conversation.setId(id);
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());

        when(chatService.getConversations())
                .thenReturn(List.of(conversation));

        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(id.toString()));
    }

    @Test
    void getConversationHistory_shouldReturnMessages()
            throws Exception {

        UUID conversationId = UUID.randomUUID();

        Message message = new Message();

        message.setId(UUID.randomUUID());
        message.setRole(MessageRole.USER);
        message.setContent("Hello");
        message.setCreatedAt(Instant.now());

        when(chatService.getConversationHistory(conversationId))
                .thenReturn(List.of(message));

        mockMvc.perform(
                get("/api/conversations/{conversationId}/messages",
                        conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content")
                        .value("Hello"))
                .andExpect(jsonPath("$[0].role")
                        .value("USER"));
    }

    @Test
    void sendMessage_shouldReturnAssistantMessage()
            throws Exception {

        UUID conversationId = UUID.randomUUID();

        Message response = new Message();

        response.setId(UUID.randomUUID());
        response.setRole(MessageRole.ASSISTANT);
        response.setContent("Hello! How can I help?");
        response.setCreatedAt(Instant.now());

        when(chatService.sendMessage(
                conversationId,
                "Hello"))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/conversations/{conversationId}/messages",
                        conversationId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role")
                        .value("ASSISTANT"))
                .andExpect(jsonPath("$.content")
                        .value("Hello! How can I help?"));
    }

    @Test
    void getConversationHistory_shouldReturnNotFound()
            throws Exception {

        UUID conversationId = UUID.randomUUID();

        when(chatService.getConversationHistory(conversationId))
                .thenThrow(new ResourceNotFoundException(
                        "Conversation not found"));

        mockMvc.perform(
                get("/api/conversations/{conversationId}/messages",
                        conversationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void sendMessage_shouldReturnNotFound()
            throws Exception {

        UUID conversationId = UUID.randomUUID();

        when(chatService.sendMessage(
                conversationId,
                "Hello"))
                .thenThrow(new ResourceNotFoundException(
                        "Conversation not found"));

        mockMvc.perform(
                post("/api/conversations/{conversationId}/messages",
                        conversationId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hello"))
                .andExpect(status().isNotFound());
    }

    @Test
    void streamMessage_shouldReturnStreamingResponse()
            throws Exception {

        UUID conversationId = UUID.randomUUID();

        when(chatService.streamMessage(
                conversationId,
                "Hello"))
                .thenReturn(
                        Flux.just(
                                "Hello ",
                                "there!"));

        mockMvc.perform(
                post("/api/conversations/{conversationId}/messages/stream",
                        conversationId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hello"))
                .andExpect(status().isOk())
                .andExpect(
                        org.springframework.test.web.servlet.result
                                .MockMvcResultMatchers
                                .content()
                                .string("Hello there!"));
    }
}