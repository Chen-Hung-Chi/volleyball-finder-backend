package com.volleyball.finder.dto;

import lombok.Data;

@Data
public class SponsorResponse {
    private Long id;
    private Long userId;
    private String name;
    private String contactEmail;
    private String phone;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private Boolean isActive;
    private Boolean useLinePay;
}
