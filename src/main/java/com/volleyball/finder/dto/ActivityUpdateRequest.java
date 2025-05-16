package com.volleyball.finder.dto;

import com.volleyball.finder.enums.NetType;
import com.volleyball.finder.validation.QuotaConstraint;
import com.volleyball.finder.validation.QuotaValidatable;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活動更新用的資料傳輸物件（DTO）
 */
@Data
@QuotaConstraint(message = "請檢查男女人數名額設定")
public class ActivityUpdateRequest implements QuotaValidatable {

    private Long id;

    /**
     * 活動標題
     */
    @NotBlank(message = "活動標題不能為空")
    @Size(max = 20, message = "活動標題長度不能超過 20 字")
    private String title;

    /**
     * 活動描述
     */
    @NotBlank(message = "活動描述不能為空")
    @Size(max = 500, message = "描述內容過長")
    private String description;

    /**
     * 活動時間
     */
    @NotNull(message = "活動時間不能為空")
    private LocalDateTime dateTime;

    /**
     * 活動時長（分鐘）
     */
    @NotNull(message = "活動時長不能為空")
    @Min(value = 1, message = "活動時間至少為 1 分鐘")
    private Integer duration;

    /**
     * 地點
     */
    @NotBlank(message = "地點不能為空")
    private String location;

    /**
     * 人數上限
     */
    @NotNull(message = "人數上限不能為空")
    @Min(value = 1, message = "人數至少為 1 人")
    @Max(value = 100, message = "人數最多 100 人")
    private Integer maxParticipants;

    /**
     * 費用（可選）
     */
    @Min(value = 0, message = "費用不能為負數")
    private Integer amount;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能為空")
    private String city;

    /**
     * 行政區
     */
    @NotBlank(message = "行政區不能為空")
    private String district;

    /**
     * 網高類型（男網、女網、混網）
     */
    @NotNull(message = "網高類型不能為空")
    private NetType netType;

    /**
     * 男生人數
     */
    @NotNull(message = "男生人數不能為空")
    @Min(value = 0, message = "男生人數不得為負數")
    private Integer maleCount;

    /**
     * 女生人數
     */
    @NotNull(message = "女生人數不能為空")
    @Min(value = 0, message = "女生人數不得為負數")
    private Integer femaleCount;

    /**
     * 女生名額限制
     */
    @Min(value = -1, message = "女生名額不得小於 -1")
    private Integer femaleQuota;

    /**
     * 男生名額限制
     */
    @Min(value = -1, message = "男生名額不得小於 -1")
    private Integer maleQuota;

    /**
     * 是否女生優先報名（選填）
     */
    private Boolean femalePriority;

    /**
     * 是否需要實名制
     */
    private Boolean requireVerification;
}