package com.retrievo.controller;

import com.retrievo.entity.Notification;
import com.retrievo.entity.User;
import com.retrievo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(Map.of("message", "Marked notification read."));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("message", "All notifications marked read."));
    }
}
