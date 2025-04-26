package com.volleyball.finder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活動參與者實體類別
 */
@Data
@TableName("activity_participants")
public class ActivityParticipants {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("activity_id")
    private Long activityId; // 活動 ID

    @TableField("user_id")
    private Long userId; // 使用者 ID

    @TableField("is_captain")
    private Boolean isCaptain; // 是否為隊長

    @TableField("is_waiting")
    private Boolean isWaiting; // 是否為候補（true = 候補，false = 正取）

    @TableField("is_deleted")
    private Boolean isDeleted; // 是否已取消（軟刪除）

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 報名時間

    @TableField(exist = false)
    private User user; // 關聯的使用者資訊（JOIN 時才填入）
}