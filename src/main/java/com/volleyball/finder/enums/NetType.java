package com.volleyball.finder.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NetType {
    MEN("MEN"),       // 男網
    WOMEN("WOMEN"),   // 女網
    MIXED("MIXED");   // 混網

    @EnumValue
    private final String value;

    NetType(String value) {
        this.value = value;
    }
}
