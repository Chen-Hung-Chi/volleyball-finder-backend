package com.volleyball.finder.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("MALE"),
    FEMALE("FEMALE");

    @EnumValue
    private final String value;

    Gender(String value) {
        this.value = value;
    }
}