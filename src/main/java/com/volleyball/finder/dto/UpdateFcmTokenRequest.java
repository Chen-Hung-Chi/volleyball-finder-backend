package com.volleyball.finder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateFcmTokenRequest {

    @NotBlank
    private String fcmToken;
}
