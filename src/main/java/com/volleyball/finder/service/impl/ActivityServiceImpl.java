package com.volleyball.finder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volleyball.finder.dto.ActivityParticipantDto;
import com.volleyball.finder.dto.ActivitySearchRequest;
import com.volleyball.finder.dto.ActivityUpdateRequest;
import com.volleyball.finder.dto.PageResponse;
import com.volleyball.finder.entity.Activity;
import com.volleyball.finder.entity.ActivityParticipants;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.enums.Gender;
import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.event.ActivityEvent;
import com.volleyball.finder.mapper.ActivityMapper;
import com.volleyball.finder.mapper.ActivityParticipantMapper;
import com.volleyball.finder.service.ActivityService;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.DateTimeUtils;
import com.volleyball.finder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper activityParticipantMapper;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private static final Integer WAITING_PARTICIPANTS = 10;

    @Override
    public Activity findById(Long id, Long userId) {
        log.info("Finding activity by id: {} for user: {}", id, userId);
        return activityMapper.findById(id, userId);
    }

    @Override
    public List<Activity> findAll(Long userId) {
        log.info("Finding all activities for user: {}", userId);
        return activityMapper.findAll(userId);
    }

    @Override
    public List<Activity> findByUserId(Long userId) {
        log.info("Finding activities for user: {}", userId);
        return activityMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public Activity create(Activity activity) {
        log.info("Creating new activity: {}", activity);
        activityMapper.insert(activity);
        log.info("Created activity with id: {}", activity.getId());

        ActivityParticipants participant = new ActivityParticipants();
        participant.setActivityId(activity.getId());
        participant.setUserId(activity.getCreatedBy());
        participant.setIsCaptain(true);  // 隊長

        activityParticipantMapper.insert(participant);

        return activity;
    }

    @Override
    @Transactional
    public Activity update(Long id, ActivityUpdateRequest activityUpdateRequest) {
        log.info("activityUpdateDto: {}", activityUpdateRequest);
        Long userId = SecurityUtils.getCurrentUserId(userService);

        Activity existing = activityMapper.selectById(id);
        if (existing == null) {
            throw new ApiException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        if (!existing.getCreatedBy().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "你不是活動發起人");
        }

        BeanUtils.copyProperties(activityUpdateRequest, existing, "id");
        activityMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting activity with id: {}", id);
        activityMapper.deleteById(id);
        log.info("Deleted activity with id: {}", id);
    }

    @Override
    @Transactional
    public void joinActivity(Long activityId, Long userId) {
        // 取出活動與使用者
        Activity activity = getExistingActivity(activityId, userId);
        User user = getExistingUser(userId);

        // 檢查是否已加入或冷卻中
        rejectIfAlreadyJoined(activityId, userId);
        rejectIfInCooldown(activityId, userId);

        // --- Determine if user should be on waiting list --- 
        boolean isWaiting = activity.getCurrentParticipants() >= activity.getMaxParticipants();
        boolean femalePriority = Boolean.TRUE.equals(activity.getFemalePriority());
        boolean isMale = user.getGender() == Gender.MALE;
        boolean femaleQuotaExistsAndNotFull = activity.getFemaleQuota() != null && activity.getFemaleQuota() > 0 &&
                activity.getFemaleCount() != null && activity.getFemaleCount() < activity.getFemaleQuota();

        // Force male to waiting list if female priority is active and female quota isn't full
        if (!isWaiting && femalePriority && isMale && femaleQuotaExistsAndNotFull) {
            log.info("Female priority active and female quota not full. Forcing male user {} to waiting list for activity {}.", userId, activityId);
            isWaiting = true;
        }
        // --- End waiting list determination ---

        // 檢查是否已滿額或違反性別限制 (考慮是否候補)
        rejectIfLimitsReached(activity, user, isWaiting);

        // 寫入參與者資料，並更新人數統計
        addParticipant(activityId, userId, isWaiting);
        activityMapper.syncCurrentParticipants(activityId);

        // 僅正取發送通知
        if (!isWaiting) {
            publishJoinNotification(activity, user);
        }
    }

    /**
     * 若活動正取與候補皆已滿，或違反性別限制，則丟出例外
     */
    private void rejectIfLimitsReached(Activity activity, User user, boolean isWaiting) {
        // 1. Check if waiting list is full (only applies if user is trying to wait)
        if (isWaiting && activity.getCurrentParticipants() >= activity.getMaxParticipants() + WAITING_PARTICIPANTS) {
            throw new ApiException(ErrorCode.ACTIVITY_FULL, "活動正取與候補皆已額滿");
        }

        // 2. Check Gender Bans (Applies whether waiting or not)
        boolean isMale = user.getGender() == Gender.MALE;
        if ((isMale && activity.getMaleQuota() != null && activity.getMaleQuota() == -1) ||
                (!isMale && activity.getFemaleQuota() != null && activity.getFemaleQuota() == -1)) {
            throw new ApiException(ErrorCode.ACTIVITY_GENDER_QUOTA_FULL, "該性別禁止報名此活動");
        }

        // 3. Check if specific gender quota is full (Only applies if joining main list, not waiting)
        if (!isWaiting) {
            if (isGenderQuotaFull(activity, user)) {
                // Throw specific error based on gender
                throw new ApiException(isMale ? ErrorCode.ACTIVITY_MALE_FULL : ErrorCode.ACTIVITY_FEMALE_FULL);
            }
        }

        // Note: Female priority check is now handled in joinActivity before this method
    }

    /**
     * 判斷使用者是否已達性別名額限制（0 表示不限制）
     */
    private boolean isGenderQuotaFull(Activity activity, User user) {
        boolean isMale = user.getGender() == Gender.MALE;
        if (isMale) {
            return activity.getMaleQuota() != null && activity.getMaleQuota() > 0 &&
                    activity.getMaleCount() != null && activity.getMaleCount() >= activity.getMaleQuota();
        } else {
            return activity.getFemaleQuota() != null && activity.getFemaleQuota() > 0 &&
                    activity.getFemaleCount() != null && activity.getFemaleCount() >= activity.getFemaleQuota();
        }
    }

    /**
     * 新增或更新參與者（包含是否為候補）
     */
    private void addParticipant(Long activityId, Long userId, boolean isWaiting) {
        activityMapper.addOrUpdateParticipant(activityId, userId, isWaiting);
    }

    /**
     * 若使用者已參加活動則丟出例外
     */
    private void rejectIfAlreadyJoined(Long activityId, Long userId) {
        if (activityMapper.isParticipant(activityId, userId)) {
            throw new ApiException(ErrorCode.ACTIVITY_JOINED);
        }
    }

    /**
     * 若使用者離開未滿 30 分鐘，則丟出例外
     */
    private void rejectIfInCooldown(Long activityId, Long userId) {
        activityMapper.findLastLeaveTime(userId, activityId)
                .map(DateTimeUtils::minutesSince)
                .filter(min -> min < 30)
                .ifPresent(min -> {
                    long left = 30 - min;
                    throw new ApiException(
                            ErrorCode.ACTIVITY_WAIT_30M,
                            "退出後需等待 30 分鐘才能重新加入，尚餘 " + left + " 分鐘");
                });
    }

    /**
     * 發送參與通知（正取才通知）
     */
    private void publishJoinNotification(Activity activity, User user) {
        String message = String.format("%s 加入了您的活動「%s」", user.getNickname(), activity.getTitle());
        eventPublisher.publishEvent(new ActivityEvent(
                this,
                activity.getId(),
                activity.getCreatedBy(),
                "新成員加入",
                message
        ));
    }


    /**
     * 查詢活動資料，若不存在則丟出錯誤
     */
    private Activity getExistingActivity(Long activityId, Long userId) {
        return Optional.ofNullable(findById(activityId, userId))
                .orElseThrow(() -> new ApiException(ErrorCode.ACTIVITY_NOT_FOUND));
    }

    /**
     * 查詢使用者資料，若不存在則丟出錯誤
     */
    private User getExistingUser(Long userId) {
        return Optional.ofNullable(userService.findById(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void leaveActivity(Long activityId, Long userId) {

        // ==== 1. 取資料 & 快速拒絕 ====
        var activity = Optional.ofNullable(findById(activityId, userId))
                .orElseThrow(() -> new ApiException(ErrorCode.ACTIVITY_NOT_FOUND));

        var participant = Optional.ofNullable(activityMapper.findParticipant(activityId, userId))
                .orElseThrow(() -> new ApiException(ErrorCode.ACTIVITY_NOT_JOINED));

        if (participant.getIsDeleted()) {
            throw new ApiException(ErrorCode.ACTIVITY_LEAVED);
        }

        // ==== 2. 更新狀態 ====
        activityMapper.removeParticipant(activityId, userId);
        activityMapper.syncCurrentParticipants(activityId);

        // ==== 2-1. 替補候補 ====
        Optional.ofNullable(activityMapper.findFirstWaiting(activityId)).ifPresent(waiting -> {
            activityMapper.updateIsWaiting(waiting.getId(), false);

            var waitingUser = Optional.ofNullable(userService.findById(waiting.getUserId()))
                    .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

            publishPromoteNotification(activity, waitingUser);
        });

        // ==== 3. 發通知 ====
        var user = Optional.ofNullable(userService.findById(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        publishLeaveNotification(activity, user);
    }

    private void publishLeaveNotification(Activity activity, User user) {
        String message = String.format("%s 退出了您的活動「%s」", user.getNickname(), activity.getTitle());
        eventPublisher.publishEvent(new ActivityEvent(
                this,
                activity.getId(),
                activity.getCreatedBy(),
                "成員退出",
                message
        ));
    }

    private void publishPromoteNotification(Activity activity, User user) {
        String message = String.format("您已成功從候補名單加入活動「%s」", activity.getTitle());
        eventPublisher.publishEvent(new ActivityEvent(
                this,
                activity.getId(),
                user.getId(),
                "候補成功",
                message
        ));
    }

    @Override
    @Transactional
    public List<ActivityParticipantDto> getActivityParticipants(Long activityId) {
        log.info("Getting participants for activity: {}", activityId);

        // 使用 var 關鍵字提高可讀性
        var participants = activityParticipantMapper.findByActivityId(activityId);

        // 使用 Optional 處理 null 檢查並結合流式處理
        Optional.ofNullable(findById(activityId, null))
                .ifPresent(activity -> activityMapper.syncCurrentParticipants(activityId));

        return participants;
    }

    @Override
    public PageResponse<Activity> search(ActivitySearchRequest request) {
        log.info("Searching activities with request: {}", request);

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        Optional.ofNullable(request.getLocation())
                .filter(StringUtils::hasText)
                .ifPresent(location -> wrapper.like(Activity::getLocation, location));

        Optional.ofNullable(request.getCity())
                .filter(StringUtils::hasText)
                .ifPresent(city -> wrapper.eq(Activity::getCity, city));

        Optional.ofNullable(request.getDistrict())
                .filter(StringUtils::hasText)
                .ifPresent(district -> wrapper.eq(Activity::getDistrict, district));

        Optional.ofNullable(request.getNetType())
                .ifPresent(netType -> wrapper.eq(Activity::getNetType, netType));

        // === 時間條件處理 ===
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = Optional.ofNullable(request.getStartDate())
                .map(LocalDate::atStartOfDay).filter(userStart -> userStart.isAfter(now)).orElse(now);
        wrapper.ge(Activity::getDateTime, startDateTime);

        Optional.ofNullable(request.getEndDate())
                .ifPresent(endDate -> wrapper.le(Activity::getDateTime, endDate.atTime(LocalTime.MAX)));

        // 時間排序（由近到遠）
        wrapper.orderByAsc(Activity::getDateTime);

        // 分頁查詢
        Page<Activity> page = new Page<>(request.getPage(), request.getLimit());
        Page<Activity> result = activityMapper.selectPage(page, wrapper);

        log.info("Found {} activities in total, returning page {} with {} items.",
                result.getTotal(), result.getCurrent(), result.getRecords().size());

        return PageResponse.of(result);
    }

    @Override
    public List<Activity> findByDate(LocalDate date) {
        log.info("Finding activities for date: {}", date);
        return activityMapper.findByDate(date);
    }

}