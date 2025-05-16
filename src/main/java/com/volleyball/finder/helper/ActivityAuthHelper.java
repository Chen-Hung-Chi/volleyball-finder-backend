package com.volleyball.finder.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volleyball.finder.entity.ActivityParticipants;
import com.volleyball.finder.mapper.ActivityParticipantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityAuthHelper {
    private final ActivityParticipantMapper activityParticipantMapper;

    public boolean isCaptain(Long activityId, Long userId) {
        return activityParticipantMapper.selectCount(
                new LambdaQueryWrapper<ActivityParticipants>()
                        .eq(ActivityParticipants::getActivityId, activityId)
                        .eq(ActivityParticipants::getUserId, userId)
                        .eq(ActivityParticipants::getIsCaptain, true)
                        .eq(ActivityParticipants::getIsDeleted, false)
        ) > 0;
    }
}