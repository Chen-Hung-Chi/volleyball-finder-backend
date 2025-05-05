package com.volleyball.finder.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Role {
    USER("USER"),
    SPONSOR("SPONSOR"),
    ADMIN("ADMIN");

    @EnumValue
    private final String value;

    Role(String value) {
        this.value = value;
    }
}