package com.volleyball.finder.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.volleyball.finder.enums.NetType;
import com.volleyball.finder.validation.QuotaConstraint;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activities")
@QuotaConstraint(message = "請檢查男女人數名額設定")
public class Activity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @NotBlank(message = "活動標題不能為空")
    @Size(max = 20, message = "活動標題長度不能超過 20 字")
    private String title; // 活動標題

    @NotBlank(message = "活動描述不能為空")
    @Size(max = 500, message = "描述內容過長")
    private String description; // 活動描述

    @NotNull(message = "活動時間不能為空")
    private LocalDateTime dateTime; // 活動時間

    @NotNull(message = "活動時長不能為空")
    @Min(value = 0, message = "活動時長不可為負數")
    private Integer duration; // 活動時長（分鐘）

    @NotBlank(message = "地點不能為空")
    private String location; // 活動地點

    @NotNull(message = "人數上限不能為空")
    @Min(value = 1, message = "人數至少為 1 人")
    @Max(value = 100, message = "人數最多 100 人")
    private Integer maxParticipants; // 報名人數上限

    private Integer currentParticipants; // 目前報名人數

    @Min(value = 0, message = "費用不能為負數")
    private Integer amount; // 費用（元）

    @NotBlank(message = "城市不能為空")
    private String city; // 城市

    @NotBlank(message = "行政區不能為空")
    private String district; // 行政區

    @NotNull(message = "建立者不能為空")
    private Long createdBy; // 建立者 User ID

    @NotNull(message = "網別不能為空")
    private NetType netType; // 網別（男網、女網、混網）

    @Min(value = 0, message = "男生人數不可為負數")
    private Integer maleCount; // 男生人數

    @Min(value = 0, message = "女生人數不可為負數")
    private Integer femaleCount; // 女生人數

    @Min(value = -1, message = "男生名額不能小於 -1")
    private Integer maleQuota; // 男生名額上限（-1: 不允許, 0: 不限制, >0: 限制人數）

    @Min(value = -1, message = "女生名額不能小於 -1")
    private Integer femaleQuota; // 女生名額上限（-1: 不允許, 0: 不限制, >0: 限制人數）

    private Boolean femalePriority; // 女生優先報名

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 建立時間

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 修改時間

}