package com.volleyball.finder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.volleyball.finder.dto.ActivityParticipantDto;
import com.volleyball.finder.entity.Activity;
import com.volleyball.finder.entity.Notification;
import com.volleyball.finder.mapper.NotificationMapper;
import com.volleyball.finder.service.ActivityService;
import com.volleyball.finder.service.NotificationService;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationMapper notificationMapper;
    private final ActivityService activityService;
    private final UserService userService;

    @Override
    @Transactional
    public void sendNotification(Notification notification) {
        notificationMapper.insert(notification);
        // --- 發送 FCM 通知 ---
        sendFcmNotification(notification);
    }

    private void sendFcmNotification(Notification notification) {
        try {
            String targetFcmToken = userService.getFcmToken(notification.getUserId());

            if (targetFcmToken == null || targetFcmToken.isEmpty()) {
                log.warn("User {} has no FCM token, skipping FCM push", notification.getUserId());
                return;
            }

            Message message = Message.builder()
                    .putData("title", notification.getTitle())
                    .putData("body", notification.getContent())
                    .setToken(targetFcmToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM push: {}", response);
        } catch (Exception e) {
            log.error("Failed to send FCM push notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<Notification> getNotification() {
        Long userId = SecurityUtils.getCurrentUserId(userService);

        return notificationMapper.selectList(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
        );
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void sendActivityReminders() {
        log.info("Starting to send activity reminders...");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Activity> tomorrowActivities = activityService.findByDate(tomorrow);
        log.info("Found {} activities scheduled for tomorrow {}", tomorrowActivities.size(), tomorrow);

        if (tomorrowActivities.isEmpty()) {
            log.info("No activities found for tomorrow, skipping reminders");
            return;
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Activity activity : tomorrowActivities) {
                List<ActivityParticipantDto> participants = activityService.getActivityParticipants(activity.getId());
                log.info("Sending reminders for activity: {} to {} participants",
                        activity.getTitle(), participants.size());

                for (ActivityParticipantDto participant : participants) {
                    executor.submit(() -> {
                        try {
                            Notification notification = new Notification();
                            notification.setUserId(participant.getUserId());
                            notification.setTitle("【活動提醒】");
                            notification.setContent("""
                                    【%s】
                                     時間：【%s】
                                     地點：【%s】
                                    """.formatted(
                                    activity.getTitle(),
                                    activity.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                    activity.getLocation()
                            ));
                            notification.setRead(false);

                            sendNotification(notification);
                            log.debug("Sent reminder to user {} for activity {}",
                                    participant.getUserId(), activity.getId());
                        } catch (Exception e) {
                            log.error("Failed to send reminder to user {} for activity {}: {}",
                                    participant.getUserId(), activity.getId(), e.getMessage(), e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error while sending activity reminders: {}", e.getMessage(), e);
        }
        log.info("Finished sending activity reminders");
    }
}