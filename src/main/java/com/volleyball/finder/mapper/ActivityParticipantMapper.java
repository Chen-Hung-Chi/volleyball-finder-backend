package com.volleyball.finder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volleyball.finder.dto.ActivityParticipantDto;
import com.volleyball.finder.entity.ActivityParticipants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityParticipantMapper extends BaseMapper<ActivityParticipants> {

    @Select("""
                SELECT ap.id, ap.activity_id, ap.user_id, ap.is_captain, ap.created_at,
                       u.line_id, u.real_name, u.nickname, u.gender, u.position, u.level,
                       u.volleyball_age, u.avatar, u.city, u.district, u.introduction,
                       u.created_at AS user_created_at, u.updated_at AS user_updated_at
                FROM activity_participants ap
                LEFT JOIN users u ON ap.user_id = u.id
                WHERE ap.activity_id = #{activityId}
                  AND ap.is_deleted = FALSE
                ORDER BY ap.is_captain DESC, ap.id
            """)
    List<ActivityParticipantDto> findByActivityId(@Param("activityId") Long activityId);
}