package com.volleyball.finder.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.volleyball.finder.service.FcmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmServiceImpl implements FcmService {

    @Override
    public void sendNotification(String targetToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .putData("title", title)
                    .putData("body", body)
                    .setToken(targetToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: {}", response);
        } catch (Exception e) {
            log.error("Failed to send FCM notification", e);
        }
    }
}
