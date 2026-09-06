package com.bondocsystems.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import com.bondocsystems.chat.exception.ResourceNotFoundException;
import com.bondocsystems.chat.model.Conversation;
import com.bondocsystems.chat.model.Message;
import com.bondocsystems.chat.model.MessageRole;
import com.bondocsystems.chat.repository.ConversationRepository;
import com.bondocsystems.chat.repository.MessageRepository;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatService chatService;

    private UUID conversationId;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();

        conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());
    }

    @Test
    void createConversation_shouldCreateAndPersistConversation() {
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Conversation result = chatService.createConversation();

        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(conversationRepository).save(result);
    }

    @Test
    void getConversations_shouldReturnAllConversations() {
        Conversation secondConversation = new Conversation();
        secondConversation.setId(UUID.randomUUID());

        when(conversationRepository.findAll())
                .thenReturn(List.of(conversation, secondConversation));

        List<Conversation> result = chatService.getConversations();

        assertThat(result)
                .hasSize(2)
                .containsExactly(conversation, secondConversation);

        verify(conversationRepository).findAll();
    }

    @Test
    void getConversationHistory_shouldReturnMessagesInRepositoryOrder() {
        Message userMessage = createMessage(
                MessageRole.USER,
                "Hello");

        Message assistantMessage = createMessage(
                MessageRole.ASSISTANT,
                "Hello! How can I help?");

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        when(messageRepository.findByConversationIdOrderByCreatedAt(
                conversationId))
                .thenReturn(List.of(userMessage, assistantMessage));

        List<Message> result =
                chatService.getConversationHistory(conversationId);

        assertThat(result)
                .hasSize(2)
                .containsExactly(userMessage, assistantMessage);

        verify(conversationRepository).findById(conversationId);
        verify(messageRepository)
                .findByConversationIdOrderByCreatedAt(conversationId);
    }

    @Test
    void getConversationHistory_shouldThrowWhenConversationDoesNotExist() {
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                chatService.getConversationHistory(conversationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found");

        verify(messageRepository, never())
                .findByConversationIdOrderByCreatedAt(any());
    }

    @Test
    void sendMessage_shouldSaveUserAndAssistantMessages() {
        String userContent = "What is Spring Boot?";
        String assistantContent = "Spring Boot is a framework for Java applications.";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        when(messageRepository.findByConversationIdOrderByCreatedAt(
                conversationId))
                .thenAnswer(invocation -> List.of(
                        createMessage(MessageRole.USER, userContent)));

        Message savedUserMessage = createMessage(
                MessageRole.USER,
                userContent);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> {
                    Message message = invocation.getArgument(0);

                    if (message.getRole() == MessageRole.USER) {
                        return savedUserMessage;
                    }

                    message.setId(UUID.randomUUID());
                    return message;
                });

        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class);

        ChatClient.CallResponseSpec callResponseSpec =
                mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt(any(
                org.springframework.ai.chat.prompt.Prompt.class)))
                .thenReturn(promptSpec);

        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(assistantContent);

        Message result =
                chatService.sendMessage(conversationId, userContent);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(result.getContent()).isEqualTo(assistantContent);
        assertThat(result.getConversation()).isEqualTo(conversation);

        verify(messageRepository, times(2)).save(any(Message.class));
        verify(chatClient).prompt(any(
                org.springframework.ai.chat.prompt.Prompt.class));
        verify(promptSpec).call();
        verify(callResponseSpec).content();
    }

    @Test
    void sendMessage_shouldThrowWhenConversationDoesNotExist() {
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                chatService.sendMessage(conversationId, "Hello"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found");

        verify(messageRepository, never()).save(any());
        verifyNoInteractions(chatClient);
    }

    @Test
    void saveAssistantMessage_shouldPersistAssistantMessage() {
        String content = "This is the assistant response.";

        chatService.saveAssistantMessage(conversation, content);

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        verify(messageRepository).save(captor.capture());

        Message savedMessage = captor.getValue();

        assertThat(savedMessage.getConversation())
                .isEqualTo(conversation);

        assertThat(savedMessage.getRole())
                .isEqualTo(MessageRole.ASSISTANT);

        assertThat(savedMessage.getContent())
                .isEqualTo(content);

        assertThat(savedMessage.getCreatedAt())
                .isNotNull();
    }

    @Test
    void streamMessage_shouldStreamAssistantResponse() {
        String userContent = "Tell me about Java.";
        String assistantContent = "Java is a programming language.";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        when(messageRepository.findByConversationIdOrderByCreatedAt(
                conversationId))
                .thenReturn(List.of(
                        createMessage(MessageRole.USER, userContent)));

        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class);

        ChatClient.StreamResponseSpec streamResponseSpec =
                mock(ChatClient.StreamResponseSpec.class);

        when(chatClient.prompt(any(
                org.springframework.ai.chat.prompt.Prompt.class)))
                .thenReturn(promptSpec);

        when(promptSpec.stream()).thenReturn(streamResponseSpec);

        when(streamResponseSpec.content())
                .thenReturn(Flux.just(
                        "Java ",
                        "is ",
                        "a programming language."));

        Flux<String> result =
                chatService.streamMessage(
                        conversationId,
                        userContent);

        StepVerifier.create(result)
                .expectNext("Java ")
                .expectNext("is ")
                .expectNext("a programming language.")
                .verifyComplete();

        verify(messageRepository, atLeastOnce()).save(any(Message.class));

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        verify(messageRepository, times(2)).save(captor.capture());

        List<Message> savedMessages = captor.getAllValues();

        Message assistantMessage = savedMessages.get(1);

        assertThat(assistantMessage.getRole())
                .isEqualTo(MessageRole.ASSISTANT);

        assertThat(assistantMessage.getContent())
                .isEqualTo(assistantContent);
    }

    @Test
    void streamMessage_shouldThrowWhenConversationDoesNotExist() {
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                chatService.streamMessage(
                        conversationId,
                        "Hello"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found");

        verifyNoInteractions(chatClient);
        verify(messageRepository, never()).save(any());
    }

    private Message createMessage(
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