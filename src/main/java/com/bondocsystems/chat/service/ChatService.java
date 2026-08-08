package com.bondocsystems.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import com.bondocsystems.chat.exception.ResourceNotFoundException;
import com.bondocsystems.chat.model.Conversation;
import com.bondocsystems.chat.model.Message;
import com.bondocsystems.chat.model.MessageRole;
import com.bondocsystems.chat.repository.ConversationRepository;
import com.bondocsystems.chat.repository.MessageRepository;

import jakarta.transaction.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private static final Logger log =
        LoggerFactory.getLogger(ChatService.class);

        
        private final ConversationRepository conversationRepository;
        
        
        private final MessageRepository messageRepository;

    public ChatService(
        ChatClient chatClient,
        ConversationRepository conversationRepository,
        MessageRepository messageRepository) {

    this.chatClient = chatClient;
    this.conversationRepository = conversationRepository;
    this.messageRepository = messageRepository;
}

    /**
     * Traditional blocking chat request.
     */
    public String chat(String message) {

        long start = System.nanoTime();

        String response = chatClient
                .prompt(message)
                .call()
                .content();

        long durationMs = (System.nanoTime() - start) / 1_000_000;

        log.info("LLM response time: {} ms", durationMs);

        return response;
    }

    /**
     * Streams the AI response as it is generated.
     */
    public Flux<String> stream(String prompt) {

        return chatClient
                .prompt(prompt)
                .stream()
                .content();
    }

    @Transactional
public com.bondocsystems.chat.model.Message sendMessage(
        UUID conversationId,
        String message) {

    Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Conversation not found"));

    // Save the user's message
    com.bondocsystems.chat.model.Message userMessage =
            new com.bondocsystems.chat.model.Message();

    userMessage.setConversation(conversation);
    userMessage.setRole(MessageRole.USER);
    userMessage.setContent(message);
    userMessage.setCreatedAt(Instant.now());

    messageRepository.save(userMessage);

    // Retrieve the conversation history
    List<com.bondocsystems.chat.model.Message> history =
            messageRepository.findByConversationIdOrderByCreatedAt(conversationId);

    // Convert database messages to Spring AI messages
    List<org.springframework.ai.chat.messages.Message> aiMessages = history.stream()
            .map(this::toAiMessage)
            .toList();

    // Send the conversation history to the LLM
    Prompt prompt = new Prompt(aiMessages);

    String aiResponse = chatClient
            .prompt(prompt)
            .call()
            .content();

    // Save the assistant's response
    com.bondocsystems.chat.model.Message assistantMessage =
            new com.bondocsystems.chat.model.Message();

    assistantMessage.setConversation(conversation);
    assistantMessage.setRole(MessageRole.ASSISTANT);
    assistantMessage.setContent(aiResponse);
    assistantMessage.setCreatedAt(Instant.now());

    messageRepository.save(assistantMessage);

    return assistantMessage;
}

private org.springframework.ai.chat.messages.Message toAiMessage(
        com.bondocsystems.chat.model.Message message) {

    return switch (message.getRole()) {
        case USER -> new UserMessage(message.getContent());
        case ASSISTANT -> new AssistantMessage(message.getContent());
    };
}

public Conversation createConversation() {
    Conversation conversation = new Conversation();

    Instant now = Instant.now();
    conversation.setCreatedAt(now);
    conversation.setUpdatedAt(now);

    return conversationRepository.save(conversation);
}

public List<Message> getConversationHistory(UUID conversationId) {
    conversationRepository.findById(conversationId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Conversation not found"));

    return messageRepository
            .findByConversationIdOrderByCreatedAt(conversationId);
}

}