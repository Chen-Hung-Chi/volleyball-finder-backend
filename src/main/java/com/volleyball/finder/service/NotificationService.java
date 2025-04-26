package com.volleyball.finder.service;

import com.volleyball.finder.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    void markAsRead(Long notificationId);
    List<Notification> getNotification();
}
