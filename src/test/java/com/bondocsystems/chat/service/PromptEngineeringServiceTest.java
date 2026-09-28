package com.bondocsystems.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import com.bondocsystems.chat.model.Conversation;
import com.bondocsystems.chat.model.Message;
import com.bondocsystems.chat.model.MessageRole;

class PromptEngineeringServiceTest {

    private final PromptEngineeringService service =
            new PromptEngineeringService();

    @Test
    void buildPrompt_shouldAddSystemInstructionsBeforeUserMessage() {
        Prompt prompt = service.buildPrompt("Explain Spring Boot.");

        assertThat(prompt.getInstructions()).hasSize(2);
        assertThat(prompt.getInstructions().get(0))
                .isInstanceOf(SystemMessage.class);
        assertThat(prompt.getInstructions().get(1))
                .isInstanceOf(UserMessage.class);
        assertThat(prompt.getInstructions().get(1).getText())
                .isEqualTo("Explain Spring Boot.");
    }

    @Test
    void buildConversationPrompt_shouldPreserveConversationOrder() {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());

        Message userMessage = createMessage(
                conversation,
                MessageRole.USER,
                "What is Java?");
        Message assistantMessage = createMessage(
                conversation,
                MessageRole.ASSISTANT,
                "Java is a programming language.");
        Message followUp = createMessage(
                conversation,
                MessageRole.USER,
                "What is Spring Boot?");

        Prompt prompt = service.buildConversationPrompt(
                List.of(userMessage, assistantMessage, followUp));

        assertThat(prompt.getInstructions()).hasSize(4);
        assertThat(prompt.getInstructions().get(0))
                .isInstanceOf(SystemMessage.class);
        assertThat(prompt.getInstructions().get(1))
                .isInstanceOf(UserMessage.class);
        assertThat(prompt.getInstructions().get(2))
                .isInstanceOf(AssistantMessage.class);
        assertThat(prompt.getInstructions().get(3))
                .isInstanceOf(UserMessage.class);

        assertThat(prompt.getInstructions().get(1).getText())
                .isEqualTo("What is Java?");
        assertThat(prompt.getInstructions().get(2).getText())
                .isEqualTo("Java is a programming language.");
        assertThat(prompt.getInstructions().get(3).getText())
                .isEqualTo("What is Spring Boot?");
    }

    private Message createMessage(
            Conversation conversation,
            MessageRole role,
            String content) {

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(Instant.now());
        return message;
    }
}
