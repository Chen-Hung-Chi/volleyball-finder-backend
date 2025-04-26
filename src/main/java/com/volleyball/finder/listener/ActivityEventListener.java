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
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRead(false);
        notificationService.sendNotification(notification);
    }
} 