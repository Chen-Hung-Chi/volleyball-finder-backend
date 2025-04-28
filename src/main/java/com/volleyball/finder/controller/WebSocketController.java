package com.volleyball.finder.controller;

import com.volleyball.finder.dto.MarkAsReadRequest;
import com.volleyball.finder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller("/api")
@RequiredArgsConstructor
public class WebSocketController {
    private final NotificationService notificationService;

    @MessageMapping("/notifications.markAsRead")
    public ResponseEntity<Void> markNotificationAsRead(MarkAsReadRequest readRequest) {
        notificationService.markAsRead(readRequest.notificationId());
        return ResponseEntity.ok().build();
    }
} 