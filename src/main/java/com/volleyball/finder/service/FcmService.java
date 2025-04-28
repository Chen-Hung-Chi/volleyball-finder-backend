package com.volleyball.finder.service;

public interface FcmService {
    void sendNotification(String targetToken, String title, String body);
}
