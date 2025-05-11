package com.volleyball.finder.listener;

import com.volleyball.finder.entity.Notification;
import com.volleyball.finder.event.ActivityEvent;
import com.volleyball.finder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleActivityEvent(ActivityEvent event) {
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .title(event.getTitle())
                .content(event.getContent())
                .isRead(false)
                .build();

        notificationService.sendNotification(notification);
    }
} 