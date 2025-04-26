package com.volleyball.finder.controller;

import com.volleyball.finder.entity.Notification;
import com.volleyball.finder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotification() {
        return ResponseEntity.ok(notificationService.getNotification());
    }
}
