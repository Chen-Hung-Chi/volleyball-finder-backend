package com.volleyball.finder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volleyball.finder.entity.Activity;
import com.volleyball.finder.entity.ActivityParticipants;
import com.volleyball.finder.enums.Gender;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Select("SELECT a.*, " +
            "CASE WHEN ap.user_id = #{userId} AND ap.is_captain = true THEN true ELSE false END as is_captain " +
            "FROM activities a " +
            "LEFT JOIN activity_participants ap ON a.id = ap.activity_id AND ap.user_id = #{userId} " +
            "WHERE a.id = #{id}")
    Activity findById(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT a.id, a.title, a.date_time, a.location, a.description, " +
            "COALESCE(ap.is_captain, false) as is_captain " +
            "FROM activities a " +
            "LEFT JOIN activity_participants ap ON a.id = ap.activity_id AND ap.user_id = #{userId} " +
            "ORDER BY a.date_time DESC")
    List<Activity> findAll(@Param("userId") Long userId);

    @Select("SELECT a.*, IF(ap.user_id = #{userId} AND ap.is_captain = true, true, false) as is_captain " +
            "FROM activities a " +
            "LEFT JOIN activity_participants ap ON a.id = ap.activity_id AND ap.user_id = #{userId} " +
            "WHERE a.created_by = #{userId} OR ap.user_id = #{userId} " +
            "ORDER BY a.date_time DESC")
    List<Activity> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM activities WHERE DATE(date_time) = #{date} ORDER BY date_time")
    List<Activity> findByDate(@Param("date") LocalDate date);

    @Select("SELECT updated_at " +
            "FROM activity_participants " +
            "WHERE user_id = #{userId} AND activity_id = #{activityId} AND is_deleted = TRUE " +
            "ORDER BY updated_at DESC " +
            "LIMIT 1")
    Optional<LocalDateTime> findLastLeaveTime(@Param("userId") Long userId, @Param("activityId") Long activityId);

    @Select("SELECT EXISTS (SELECT 1 FROM activity_participants WHERE activity_id = #{activityId} AND user_id = #{userId} AND is_deleted = FALSE)")
    boolean isParticipant(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Select("SELECT * FROM activity_participants WHERE activity_id = #{activityId} AND user_id = #{userId} LIMIT 1")
    ActivityParticipants findParticipant(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Insert("""
                INSERT INTO activity_participants (activity_id, user_id, is_waiting)
                VALUES (#{activityId}, #{userId}, #{isWaiting})
                ON DUPLICATE KEY UPDATE is_deleted = FALSE, is_waiting = #{isWaiting}
            """)
    void addOrUpdateParticipant(@Param("activityId") Long activityId,
                                @Param("userId") Long userId,
                                @Param("isWaiting") boolean isWaiting);

    @Update("UPDATE activity_participants SET is_deleted = TRUE, updated_at = NOW() WHERE activity_id = #{activityId} AND user_id = #{userId} AND is_deleted = FALSE")
    void removeParticipant(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Update("""
                UPDATE activities a
                SET
                    a.current_participants = (
                        SELECT COUNT(*)
                        FROM activity_participants ap
                        JOIN users u ON ap.user_id = u.id
                        WHERE ap.activity_id = #{activityId}
                          AND ap.is_deleted = FALSE
                    ),
                    a.male_count = (
                        SELECT COUNT(*)
                        FROM activity_participants ap
                        JOIN users u ON ap.user_id = u.id
                        WHERE ap.activity_id = #{activityId}
                          AND ap.is_deleted = FALSE
                          AND u.gender = 'MALE'
                    ),
                    a.female_count = (
                        SELECT COUNT(*)
                        FROM activity_participants ap
                        JOIN users u ON ap.user_id = u.id
                        WHERE ap.activity_id = #{activityId}
                          AND ap.is_deleted = FALSE
                          AND u.gender = 'FEMALE'
                    )
                WHERE a.id = #{activityId}
            """)
    void syncCurrentParticipants(@Param("activityId") Long activityId);

    @Select("""
                SELECT * FROM activity_participants
                WHERE activity_id = #{activityId}
                  AND is_waiting = TRUE
                  AND is_deleted = FALSE
                ORDER BY created_at
                LIMIT 1
            """)
    ActivityParticipants findFirstWaiting(@Param("activityId") Long activityId);

    @Update("""
                UPDATE activity_participants
                SET is_waiting = #{isWaiting}
                WHERE id = #{id}
            """)
    void updateIsWaiting(@Param("id") Long id, @Param("isWaiting") boolean isWaiting);

    @Select("""
            SELECT COUNT(*)
            FROM activity_participants ap
            JOIN users u ON ap.user_id = u.id
            WHERE ap.activity_id = #{activityId}
              AND ap.is_deleted = FALSE
              AND u.gender = #{gender}
            """)
    int countParticipantsByGender(@Param("activityId") Long activityId,
                                  @Param("gender") Gender gender);
}