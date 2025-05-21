package com.volleyball.finder.service.impl;

import com.volleyball.finder.entity.Activity;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.enums.Gender;
import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.event.ActivityEvent;
import com.volleyball.finder.mapper.ActivityMapper;
import com.volleyball.finder.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
/**
 * 單元測試：ActivityServiceImpl.joinActivity
 */
@ExtendWith(MockitoExtension.class)
// 考慮移除或僅在必要的測試上使用
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityServiceImplTest {

    // ===== 依賴 =====
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    // ===== 被測物件 =====
    @InjectMocks
    private ActivityServiceImpl service;

    private Activity activity;
    private User user;
    private User femaleUser; // Add a female user for testing
    private static final Integer WAITING_PARTICIPANTS = 10;
    @BeforeEach
    void setUp() {
        // 一個尚未額滿的活動
        activity = new Activity();
        activity.setId(1L);
        activity.setCreatedBy(99L);
        activity.setTitle("週末排球揪團");
        activity.setMaxParticipants(6);
        activity.setCurrentParticipants(3);
        activity.setFemalePriority(false);
        activity.setMaleQuota(0); // 預設不限制
        activity.setFemaleQuota(0); // 預設不限制
        activity.setMaleCount(0); // Initialize male count
        activity.setFemaleCount(0); // Initialize female count
        activity.setDateTime(LocalDateTime.now().plusDays(1)); // Future activity

        // 一位男生使用者
        user = new User();
        user.setId(88L);
        user.setNickname("Tom");
        user.setGender(Gender.MALE);

        // 一位女生使用者
        femaleUser = new User();
        femaleUser.setId(77L);
        femaleUser.setNickname("Mary");
        femaleUser.setGender(Gender.FEMALE);

        // Common mocking for successful finds (override in specific tests if needed)
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity);
        when(userService.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(user.getId())) return user;
            if (id.equals(femaleUser.getId())) return femaleUser;
            return null;
        });
        when(activityMapper.isParticipant(eq(activity.getId()), anyLong())).thenReturn(false);
        when(activityMapper.findLastLeaveTime(anyLong(), eq(activity.getId()))).thenReturn(Optional.empty());
    }

    // ===== 成功加入 =====
    @Test
    void joinActivity_success_shouldAddParticipantAndPublishEvent() {
        // Arrange (Already done in setup for male user)

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId()));

        // Assert
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), false, false);
        verify(activityMapper).syncCurrentParticipants(activity.getId());

        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ActivityEvent evt = captor.getValue();
        assertEquals(activity.getId(), evt.getActivityId());
        assertEquals(activity.getCreatedBy(), evt.getUserId()); // Should be activity creator
        assertEquals("新成員加入", evt.getTitle());
        assertTrue(evt.getContent().contains(user.getNickname()));
    }

    // ===== 活動不存在 =====
    @Test
    void joinActivity_activityNotFound_shouldThrow() {
        // Arrange
        when(activityMapper.findById(eq(123L), anyLong())).thenReturn(null);

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(123L, user.getId()));

        assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 使用者不存在 =====
    @Test
    void joinActivity_userNotFound_shouldThrow() {
        // Arrange
        when(userService.findById(111L)).thenReturn(null); // Non-existent user

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), 111L));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 已參加過 =====
    @Test
    void joinActivity_alreadyJoined_shouldThrow() {
        // Arrange
        when(activityMapper.isParticipant(activity.getId(), user.getId())).thenReturn(true);

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), user.getId()));

        assertEquals(ErrorCode.ACTIVITY_JOINED, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 活動已額滿 =====
    @Test
    void joinActivity_activityFull_shouldAddToWaiting() {
        // Arrange
        activity.setCurrentParticipants(activity.getMaxParticipants()); // Make activity full (6/6)
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); // <<< Ensure service uses this state

        // Act & Assert
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId()));
        
        // Should add to waiting list
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), true, false);
        verify(activityMapper).syncCurrentParticipants(activity.getId()); // Should still sync
        verify(eventPublisher, never()).publishEvent(any(ActivityEvent.class)); // No event for waiting
    }

    // ===== 30 分鐘冷卻 =====
    @Test
    void joinActivity_withinCooldown_shouldThrow() {
        // Arrange
        // LocalDateTime recentLeaveTime = LocalDateTime.now().minusMinutes(15); // INCORRECT: Uses system default time
        
        // CORRECT: Calculate what the LocalDateTime would be in Taipei for an event 15 mins ago.
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        LocalDateTime recentLeaveTimeInTaipei = LocalDateTime.ofInstant(
            Instant.now().minus(15, ChronoUnit.MINUTES), // Get instant 15 mins ago
            taipeiZone                                   // Convert to LocalDateTime in Taipei zone
        );

        // Remove potentially confounding findById mock for this specific test
        when(activityMapper.findLastLeaveTime(eq(user.getId()), eq(activity.getId())))
                .thenReturn(Optional.of(recentLeaveTimeInTaipei)); // Use Taipei-equivalent time

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), user.getId()));

        assertEquals(ErrorCode.ACTIVITY_WAIT_30M, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 30 分鐘冷卻 (剛好 30 分鐘) =====
    @Test
    void joinActivity_exactlyCooldown_shouldAllowJoin() {
        // Arrange
        LocalDateTime leaveTime = LocalDateTime.now().minusMinutes(30);
        when(activityMapper.findLastLeaveTime(user.getId(), activity.getId()))
                .thenReturn(Optional.of(leaveTime));

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId()));

        // Assert
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), false, false);
        verify(eventPublisher).publishEvent(any(ActivityEvent.class));
    }

    // ===== 30 分鐘冷卻 (超過 30 分鐘) =====
    @Test
    void joinActivity_afterCooldown_shouldAllowJoin() {
        // Arrange
        LocalDateTime leaveTime = LocalDateTime.now().minusMinutes(31);
        when(activityMapper.findLastLeaveTime(user.getId(), activity.getId()))
                .thenReturn(Optional.of(leaveTime));

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId()));

        // Assert
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), false, false);
        verify(eventPublisher).publishEvent(any(ActivityEvent.class));
    }


    // ===== 男生名額已滿 =====
    @Test
    void joinActivity_maleQuotaFull_shouldThrow() {
        // Arrange
        activity.setMaleQuota(2); // Limit males to 2
        activity.setMaleCount(2);
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(5);
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); // <<< Ensure service uses this state
        // We don't need the countParticipantsByGender mock anymore if service uses activity counts
        // when(activityMapper.countParticipantsByGender(activity.getId(), Gender.MALE)).thenReturn(2);

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), user.getId())); // Male user tries to join

        assertEquals(ErrorCode.ACTIVITY_MALE_FULL, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 女生名額已滿 =====
    @Test
    void joinActivity_femaleQuotaFull_shouldThrow() {
        // Arrange
        activity.setFemaleQuota(1); // Limit females to 1
        activity.setFemaleCount(1);
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(5);
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); // <<< Ensure service uses this state
        // when(activityMapper.countParticipantsByGender(the activity.getId(), Gender.FEMALE)).thenReturn(1);

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), femaleUser.getId())); // Female user tries to join

        assertEquals(ErrorCode.ACTIVITY_FEMALE_FULL, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 限制男生 =====
    @Test
    void joinActivity_maleBanned_shouldThrow() {
        // Arrange
        activity.setMaleQuota(-1); // Ban males
        activity.setFemaleQuota(0); // Allow females
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(5);

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), user.getId())); // Male user tries to join

        assertEquals(ErrorCode.ACTIVITY_GENDER_QUOTA_FULL, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 限制女生 =====
    @Test
    void joinActivity_femaleBanned_shouldThrow() {
        // Arrange
        activity.setFemaleQuota(-1); // Ban females
        activity.setMaleQuota(0); // Allow males
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(5);

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), femaleUser.getId())); // Female user tries to join

        assertEquals(ErrorCode.ACTIVITY_GENDER_QUOTA_FULL, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== 女生優先 - 女生報名 (名額未滿) =====
    @Test
    void joinActivity_femalePriority_femaleJoins_quotaAvailable_shouldSucceed() {
        // Arrange
        activity.setFemalePriority(true);
        activity.setFemaleQuota(3);
        activity.setFemaleCount(1);
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(5);
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); // <<< Ensure service uses this state
        // when(activityMapper.countParticipantsByGender(activity.getId(), Gender.FEMALE)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), femaleUser.getId())); // Female joins

        // Assert
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), femaleUser.getId(), false, false);
        verify(eventPublisher).publishEvent(any(ActivityEvent.class));
    }

    // ===== 女生優先 - 男生報名 (女生名額未滿) =====
    @Test
    void joinActivity_femalePriority_maleJoins_femaleQuotaAvailable_shouldAddToWaiting() {
        // Arrange
        activity.setFemalePriority(true);
        activity.setFemaleQuota(3);
        activity.setFemaleCount(1);
        activity.setMaxParticipants(6);
        activity.setCurrentParticipants(4);
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); // <<< Ensure service uses this state
        // when(activityMapper.countParticipantsByGender(activity.getId(), Gender.FEMALE)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId())); // Male joins

        // Assert: Male should be added to waiting list because female quota isn't full
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), true, false); // isWaiting = true
        // <<< Event should NOT be published for waiting list users >>>
        verify(eventPublisher, never()).publishEvent(any(ActivityEvent.class));
    }

    // ===== 女生優先 - 男生報名 (女生名額已滿, 總名額未滿) =====
    @Test
    void joinActivity_femalePriority_maleJoins_femaleQuotaFull_shouldJoinNormally() {
        // Arrange
        activity.setFemalePriority(true);
        activity.setFemaleQuota(2);
        activity.setFemaleCount(2);
        activity.setMaxParticipants(6);
        activity.setCurrentParticipants(4);
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); // <<< Ensure service uses this state
        // when(activityMapper.countParticipantsByGender(activity.getId(), Gender.FEMALE)).thenReturn(2);

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId())); // Male joins

        // Assert: Male should join normally as female quota is full
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), false, false); // isWaiting = false
        verify(eventPublisher).publishEvent(any(ActivityEvent.class));
    }

    // ===== 女生優先 - 男生報名 (女生名額已滿, 總名額也滿) =====
    @Test
    void joinActivity_femalePriority_maleJoins_femaleQuotaFull_activityFull_shouldAddToWaiting() {
        // Arrange
        activity.setFemalePriority(true);
        activity.setFemaleQuota(2);
        activity.setFemaleCount(2);
        activity.setMaxParticipants(4);
        activity.setCurrentParticipants(4);
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity);

        // Act
        assertDoesNotThrow(() -> service.joinActivity(activity.getId(), user.getId())); // Male joins

        // Assert: Male should be added to waiting list
        verify(activityMapper).joinOrUpdateParticipant(activity.getId(), user.getId(), true, false); // isWaiting = true
        verify(activityMapper).syncCurrentParticipants(activity.getId());
        verify(eventPublisher, never()).publishEvent(any(ActivityEvent.class)); // No event for waiting
    }

    // ===== 等待列表已滿 =====
    @Test
    void joinActivity_waitingListFull_shouldThrow() {
        // Arrange
        activity.setMaxParticipants(6);
        activity.setCurrentParticipants(6 + WAITING_PARTICIPANTS); // Main + Waiting list is full (16)
        when(activityMapper.findById(eq(activity.getId()), anyLong())).thenReturn(activity); 

        // Act & Assert
        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.joinActivity(activity.getId(), user.getId()));

        assertEquals(ErrorCode.ACTIVITY_FULL, ex.code());
        verify(activityMapper, never()).joinOrUpdateParticipant(anyLong(), anyLong(), anyBoolean(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any());
    }
}