package com.bondocsystems.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.access.AccessDeniedException;
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

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int CONVERSATION_TITLE_MAX_LENGTH = 60;

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PromptEngineeringService promptEngineeringService;
    private final FinancialContextClient financialContextClient;

    public ChatService(
            ChatClient chatClient,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            PromptEngineeringService promptEngineeringService,
            FinancialContextClient financialContextClient) {
        this.chatClient = chatClient;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.promptEngineeringService = promptEngineeringService;
        this.financialContextClient = financialContextClient;
    }

    public String chat(String message) {
        long start = System.nanoTime();
        String response = chatClient.prompt(message).call().content();
        log.info("LLM response time: {} ms", (System.nanoTime() - start) / 1_000_000);
        return response;
    }

    public Flux<String> stream(String prompt) {
        return chatClient.prompt(promptEngineeringService.buildPrompt(prompt)).stream().content();
    }

    @Transactional
    public Message sendMessage(UUID conversationId, String message, String customerId, String bearerToken) {
        Conversation conversation = getOwnedConversation(conversationId, customerId);
        Message userMessage = createMessage(conversation, MessageRole.USER, message);
        messageRepository.save(userMessage);

        if (conversation.getTitle() == null) {
            conversation.setTitle(generateConversationTitle(message));
            conversation.setUpdatedAt(Instant.now());
            conversationRepository.save(conversation);
        }

        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAt(conversationId);
        String financialContext = financialContextClient.getContext(bearerToken);
        Prompt prompt = promptEngineeringService.buildConversationPrompt(history, financialContext);
        String aiResponse = chatClient.prompt(prompt).call().content();

        Message assistantMessage = createMessage(conversation, MessageRole.ASSISTANT, aiResponse);
        return messageRepository.save(assistantMessage);
    }

    @Transactional
    public Conversation createConversation(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new AccessDeniedException("A customer identity is required");
        }
        Instant now = Instant.now();
        Conversation conversation = new Conversation();
        conversation.setCustomerId(customerId);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public List<Message> getConversationHistory(UUID conversationId, String customerId) {
        getOwnedConversation(conversationId, customerId);
        return messageRepository.findByConversationIdOrderByCreatedAt(conversationId);
    }

    @Transactional
    public List<Conversation> getConversations(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new AccessDeniedException("A customer identity is required");
        }
        return conversationRepository.findAllByCustomerIdOrderByUpdatedAtDesc(customerId);
    }

    public Flux<String> streamMessage(UUID conversationId, String message, String customerId, String bearerToken) {
        Conversation conversation = getOwnedConversation(conversationId, customerId);
        messageRepository.save(createMessage(conversation, MessageRole.USER, message));

        if (conversation.getTitle() == null) {
            conversation.setTitle(generateConversationTitle(message));
            conversation.setUpdatedAt(Instant.now());
            conversationRepository.save(conversation);
        }

        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAt(conversationId);
        String financialContext = financialContextClient.getContext(bearerToken);
        Prompt prompt = promptEngineeringService.buildConversationPrompt(history, financialContext);
        StringBuilder responseBuilder = new StringBuilder();

        return chatClient.prompt(prompt).stream().content()
                .doOnNext(responseBuilder::append)
                .doOnComplete(() -> saveAssistantMessage(conversation, responseBuilder.toString()))
                .doOnError(error -> log.error("AI response streaming failed for conversation {}", conversationId, error));
    }

    private Conversation getOwnedConversation(UUID conversationId, String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new AccessDeniedException("A customer identity is required");
        }
        return conversationRepository.findByIdAndCustomerId(conversationId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    private Message createMessage(Conversation conversation, MessageRole role, String content) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(Instant.now());
        return message;
    }

    @Transactional
    public void saveAssistantMessage(Conversation conversation, String content) {
        messageRepository.save(createMessage(conversation, MessageRole.ASSISTANT, content));
    }

    private String generateConversationTitle(String message) {
        String title = message.replaceAll("\\s+", " ").trim();
        if (title.length() <= CONVERSATION_TITLE_MAX_LENGTH) {
            return title;
        }
        String truncated = title.substring(0, CONVERSATION_TITLE_MAX_LENGTH - 3);
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > 0) {
            truncated = truncated.substring(0, lastSpace);
        }
        return truncated + "...";
    }
}