package com.volleyball.finder.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> items;
    private long total;
    private long page;
    private long limit;
    private long totalPages;

}