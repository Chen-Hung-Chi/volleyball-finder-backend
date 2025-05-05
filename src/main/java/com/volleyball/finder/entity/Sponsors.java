package com.volleyball.finder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Sponsors {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    @NotBlank
    private String name;
    private String contactEmail;
    @NotBlank
    private String phone;
    private String description;
    @NotBlank
    private String logoUrl;
    private String websiteUrl;
    private Boolean isActive;
    private Boolean useLinePay;
    private String linePayChannelId;
    private String linePayChannelSecret;
    private String linePayMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

