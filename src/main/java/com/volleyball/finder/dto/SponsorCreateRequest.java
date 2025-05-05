package com.volleyball.finder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SponsorCreateRequest {

    @NotBlank
    private String name;

    private String contactEmail;

    @NotBlank
    private String phone;

    private String description;

    @NotBlank
    private String logoUrl;

    private String websiteUrl;

    private Boolean isActive;

    private Boolean useLinePay;

    private String linePayChannelId;

    private String linePayChannelSecret;

    private String linePayMode;
}
