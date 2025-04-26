package com.volleyball.finder.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Level {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED"),
    EXPERT("EXPERT");

    @EnumValue
    private final String value;

    Level(String value) {
        this.value = value;
    }
}