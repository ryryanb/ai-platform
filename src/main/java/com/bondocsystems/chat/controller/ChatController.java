package com.bondocsystems.chat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bondocsystems.chat.model.Conversation;
import com.bondocsystems.chat.model.Message;
import com.bondocsystems.chat.service.ChatService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/../chat")
    public String chat(@RequestParam String message) {
        return chatService.chat(message);
    }

    @GetMapping(value = "/../stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatService.stream(message);
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createConversation(customerId(jwt)));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody String message,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chatService.sendMessage(conversationId, message, customerId(jwt), bearerToken(jwt)));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<Message>> getConversationHistory(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chatService.getConversationHistory(conversationId, customerId(jwt)));
    }

    @GetMapping
    public ResponseEntity<List<Conversation>> getConversations(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chatService.getConversations(customerId(jwt)));
    }

    @PostMapping(value = "/{conversationId}/messages/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamMessage(
            @PathVariable UUID conversationId,
            @RequestBody String message,
            @AuthenticationPrincipal Jwt jwt) {
        return chatService.streamMessage(conversationId, message, customerId(jwt), bearerToken(jwt));
    }

    private String bearerToken(Jwt jwt) {
        if (jwt == null || jwt.getTokenValue() == null || jwt.getTokenValue().isBlank()) {
            throw new AccessDeniedException("A customer access token is required");
        }
        return "Bearer " + jwt.getTokenValue();
    }

    private String customerId(Jwt jwt) {
        String customerId = jwt == null ? null : jwt.getClaimAsString("customerId");
        if (customerId == null || customerId.isBlank()) {
            throw new AccessDeniedException("The access token does not contain a customer identity");
        }
        return customerId;
    }
}