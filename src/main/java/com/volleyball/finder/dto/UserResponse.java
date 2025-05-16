package com.volleyball.finder.dto;

import com.volleyball.finder.enums.Gender;
import com.volleyball.finder.enums.Level;
import com.volleyball.finder.enums.Position;
import com.volleyball.finder.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Role role;
    private String nickname;
    private Boolean isVerified;
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
