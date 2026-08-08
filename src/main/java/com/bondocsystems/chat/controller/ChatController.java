package com.bondocsystems.chat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping(
            value = "/../stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> stream(@RequestParam String message) {
        return chatService.stream(message);
    }

    @PostMapping
public ResponseEntity<Conversation> createConversation() {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(chatService.createConversation());
}

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody String message) {

        return ResponseEntity.ok(
                chatService.sendMessage(conversationId, message)
        );
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<Message>> getConversationHistory(
            @PathVariable UUID conversationId) {

        return ResponseEntity.ok(
                chatService.getConversationHistory(conversationId)
        );
    }

    
}