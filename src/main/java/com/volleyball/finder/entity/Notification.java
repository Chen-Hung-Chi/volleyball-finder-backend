package com.volleyball.finder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
@Builder
public class Notification {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}