package com.volleyball.finder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.volleyball.finder.enums.Gender;
import com.volleyball.finder.enums.Level;
import com.volleyball.finder.enums.Position;
import com.volleyball.finder.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @NotBlank
    private String lineId;
    private String fcmToken;
    private Role role;
    private String realName;
    private String nickname;
    private Gender gender;
    private Position position;
    private Level level;
    private Integer volleyballAge;
    private String avatar;
    private String city;
    private String district;
    private String introduction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 