package com.volleyball.finder.controller;

import com.volleyball.finder.entity.Notification;
import com.volleyball.finder.security.CustomUserDetails;
import com.volleyball.finder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * 單筆設為已讀
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markSingle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 批次設為已讀
     * PUT /api/notifications/read   body: [1,2,3]
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markBatch(@AuthenticationPrincipal CustomUserDetails user) {
        notificationService.markAsReadBatch(user.getId());
        return ResponseEntity.noContent().build();
    }
}
