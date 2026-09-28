package com.bondocsystems.chat.service;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import com.bondocsystems.chat.model.MessageRole;

/**
 * Builds prompts sent to the language model.
 *
 * <p>This layer owns system instructions and the translation of persisted
 * conversation messages into Spring AI messages so prompt behavior can evolve
 * independently from conversation and persistence logic.</p>
 */
@Service
public class PromptEngineeringService {

    private static final String SYSTEM_INSTRUCTION = """
            You are a helpful AI assistant.
            Provide clear, accurate, and concise answers.
            When explaining technical topics, prefer practical examples and
            explain important trade-offs when they are relevant.
            """;

    /**
     * Builds a prompt for a standalone user message.
     */
    public Prompt buildPrompt(String userMessage) {
        return buildPrompt(List.of(new UserMessage(userMessage)));
    }

    /**
     * Builds a prompt containing the system instructions and conversation context.
     */
    public Prompt buildConversationPrompt(
            List<com.bondocsystems.chat.model.Message> conversationHistory) {

        List<Message> messages = conversationHistory.stream()
                .map(this::toAiMessage)
                .toList();

        return buildPrompt(messages);
    }

    private Prompt buildPrompt(List<Message> messages) {
        List<Message> promptMessages = new java.util.ArrayList<>();
        promptMessages.add(new SystemMessage(SYSTEM_INSTRUCTION));
        promptMessages.addAll(messages);

        return new Prompt(promptMessages);
    }

    private Message toAiMessage(
            com.bondocsystems.chat.model.Message message) {

        return switch (message.getRole()) {
            case USER -> new UserMessage(message.getContent());
            case ASSISTANT -> new AssistantMessage(message.getContent());
        };
    }
}
