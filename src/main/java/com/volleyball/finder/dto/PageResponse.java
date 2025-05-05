package com.volleyball.finder.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> items;
    private long total;
    private long page;
    private long limit;
    private long totalPages;

    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setItems(page.getRecords());
        response.setTotal(page.getTotal());
        response.setPage(page.getCurrent());
        response.setLimit(page.getSize());
        response.setTotalPages(page.getPages());
        return response;
    }
}