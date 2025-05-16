package com.volleyball.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrivateResponse {
    private Long id;
    private String realName;
    private String phone;
}
