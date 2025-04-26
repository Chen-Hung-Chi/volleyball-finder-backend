package com.volleyball.finder.service;

import com.volleyball.finder.dto.ActivityParticipantDto;
import com.volleyball.finder.dto.ActivitySearchRequest;
import com.volleyball.finder.dto.ActivityUpdateDto;
import com.volleyball.finder.dto.PageResponse;
import com.volleyball.finder.entity.Activity;

import java.time.LocalDate;
import java.util.List;

public interface ActivityService {
    Activity findById(Long id, Long userId);
    List<Activity> findAll(Long userId);
    List<Activity> findByUserId(Long userId);
    Activity create(Activity activity);
    Activity update(Long id, ActivityUpdateDto activityUpdateDto);
    void delete(Long id);
    void joinActivity(Long activityId, Long userId);
    void leaveActivity(Long activityId, Long userId);
    PageResponse<Activity> search(ActivitySearchRequest request);
    List<ActivityParticipantDto> getActivityParticipants(Long activityId);
    List<Activity> findByDate(LocalDate date);

}