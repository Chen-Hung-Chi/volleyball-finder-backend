package com.volleyball.finder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final ActivityService activityService;
    private final UserService userService;
    private final ExecutorService notificationExecutor;   // 由 ThreadConfig 注入

    /*──────────────────────────── 通知 CRUD ────────────────────────────*/

    @Override
    public void sendNotification(Notification notification) {
        notificationMapper.insert(notification);
        pushFcm(notification);
    }

    @Override
    public List<Notification> getNotification() {
        Long userId = SecurityUtils.getCurrentUserId(userService);
        return notificationMapper.selectList(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at"));
    }


    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationMapper.update(
                null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, true)
        );
        if (updated == 0) {
            log.warn("User {} tried to mark unknown / unauthorized notification {} as read", userId, notificationId);
            throw new IllegalArgumentException("無此通知或無權限");
        }
    }

    @Override
    @Transactional
    public void markAsReadBatch(Long userId) {
        notificationMapper.update(
                null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, true)
        );
    }

    /*──────────────────────────── 每日活動提醒 ────────────────────────────*/

    /**
     * 每日 00:00 觸發，Asia/Taipei
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void sendActivityReminders() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Activity> activities = activityService.findByDate(tomorrow);
        log.info("Found {} activities scheduled for {}", activities.size(), tomorrow);

        if (activities.isEmpty()) return;

        // 一次查出所有參與者，避免 N+1
        Map<Long, List<ActivityParticipantDto>> participantMap =
                activityService.getParticipantsByActivityIds(
                                activities.stream().map(Activity::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(ActivityParticipantDto::getActivityId));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Activity act : activities) {
            List<ActivityParticipantDto> participants =
                    participantMap.getOrDefault(act.getId(), List.of());
            if (participants.isEmpty()) continue;

            String title = "【活動提醒】";
            String body = """
                    【%s】
                    時間：【%s】
                    地點：【%s】
                    """.formatted(
                    act.getTitle(),
                    act.getDateTime().format(fmt),
                    act.getLocation());

            // 交由虛擬執行緒批量送出
            participants.forEach(p -> notificationExecutor.submit(() -> pushReminder(p, title, body, act.getId())));
        }
        log.info("Finished scheduling activity reminders tasks.");
    }

    private void pushReminder(ActivityParticipantDto p, String title, String body, Long activityId) {
        try {
            Notification n = Notification.builder()
                    .userId(p.getUserId())
                    .title(title)
                    .content(body)
                    .isRead(false)
                    .build();
            sendNotification(n);
            log.debug("Reminder sent to user {} for activity {}", p.getUserId(), activityId);
        } catch (Exception e) {
            log.error("Failed reminder to user {} for activity {} : {}", p.getUserId(), activityId, e.getMessage(), e);
        }
    }

    /*──────────────────────────── FCM PUSH ────────────────────────────*/

    private void pushFcm(Notification n) {
        try {
            String token = userService.getFcmToken(n.getUserId());
            if (token == null || token.isEmpty()) {
                log.warn("User {} has no FCM token, skipping push", n.getUserId());
                return;
            }

            Message msg = Message.builder()
                    .putData("title", n.getTitle())
                    .putData("body", n.getContent())
                    .setToken(token)
                    .build();

            String resp = FirebaseMessaging.getInstance().send(msg);
            log.info("FCM push OK id={}", resp);
        } catch (Exception e) {
            log.error("FCM push FAIL user {} : {}", n.getUserId(), e.getMessage(), e);
        }
    }
}