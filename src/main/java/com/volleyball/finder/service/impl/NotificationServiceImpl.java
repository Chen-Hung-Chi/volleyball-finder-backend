package com.volleyball.finder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Override
    @Transactional
    public void sendNotification(Notification notification) {
        notificationMapper.insert(notification);
        // 直接發送 WebSocket 通知
        messagingTemplate.convertAndSendToUser(
                notification.getUserId().toString(),
                "/queue/notifications",
                notification
        );
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null) {
            notification.setRead(true);
            notificationMapper.updateById(notification);
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

    @Scheduled(cron = "0 * * * * *")
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

        // 使用虛擬線程執行器
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Activity activity : tomorrowActivities) {
                List<ActivityParticipantDto> participants = activityService.getActivityParticipants(activity.getId());
                log.info("Sending reminders for activity: {} to {} participants",
                        activity.getTitle(), participants.size());

                for (ActivityParticipantDto participant : participants) {
                    // 提交任務到虛擬線程
                    executor.submit(() -> {
                        try {
                            Notification notification = new Notification();
                            notification.setUserId(participant.getUserId());
                            notification.setTitle("活動提醒");
                            notification.setContent("""
                                    提醒：您報名的活動「%s」將在明天舉行
                                    時間：%s
                                    地點：%s
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