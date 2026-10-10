package com.retrievo.controller;

import com.retrievo.entity.Conversation;
import com.retrievo.entity.Message;
import com.retrievo.entity.User;
import com.retrievo.service.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private MessagingService messagingService;

    @GetMapping
    public ResponseEntity<List<Conversation>> getUserConversations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(messagingService.getUserConversations(user));
    }

    @PostMapping
    public ResponseEntity<Conversation> startConversation(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Long> body
    ) {
        Long itemId = body.get("itemId");
        return ResponseEntity.ok(messagingService.startOrGetConversation(itemId, user));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(messagingService.getConversationMessages(id, user));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");
        return ResponseEntity.ok(messagingService.sendMessage(id, user, content));
    }
}
