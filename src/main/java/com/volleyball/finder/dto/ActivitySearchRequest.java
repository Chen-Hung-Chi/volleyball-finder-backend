package com.volleyball.finder.dto;

import com.volleyball.finder.enums.NetType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 活動搜尋請求 DTO
 */
@Data
public class ActivitySearchRequest {

    private Integer page = 1;
    private Integer limit = 10;

    /** 地點（模糊搜尋） */
    private String location;

    /** 城市 */
    private String city;

    /** 行政區 */
    private String district;

    /** 起始日期（含） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 結束日期（含） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 網高類型（男網、女網、混網） */
    private NetType netType;
}