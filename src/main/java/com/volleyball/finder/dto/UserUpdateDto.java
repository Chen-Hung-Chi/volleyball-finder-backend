package com.volleyball.finder.dto;

import com.volleyball.finder.enums.Gender;
import com.volleyball.finder.enums.Level;
import com.volleyball.finder.enums.Position;
import lombok.Data;

@Data
public class UserUpdateDto {
    private String realName;
    private String nickname;
    private String avatar;
    private String introduction;
    private Gender gender;
    private Position position;
    private Level level;
    private Integer volleyballAge;
    private String city;
    private String district;
}