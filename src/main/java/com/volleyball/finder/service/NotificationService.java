package com.volleyball.finder.service;

import com.volleyball.finder.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);

    List<Notification> getNotification();

    void markAsRead(Long notificationId, Long userId);

    void markAsReadBatch(Long userId);
}
