package com.volleyball.finder.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Position {
    NONE("NONE"),
    SPIKER("SPIKER"),
    SETTER("SETTER"),
    LIBERO("LIBERO");

    @EnumValue
    private final String value;

    Position(String value) {
        this.value = value;
    }
}