package com.volleyball.finder.dto;

import com.volleyball.finder.enums.Gender;
import com.volleyball.finder.enums.Level;
import com.volleyball.finder.enums.Position;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityParticipantDto {
    private Long id;
    private Long activityId;
    private Long userId;
    private Boolean isCaptain;
    private LocalDateTime createdAt;

    // user 資訊
    private String realName;
    private String nickname;
    private Gender gender;
    private Position position;
    private Level level;
    private Integer volleyballAge;
    private String avatar;
}
