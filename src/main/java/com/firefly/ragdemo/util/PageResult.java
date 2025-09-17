package com.firefly.ragdemo.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> {

    private int page; // 1-based index

    private int limit;

    private long total;

    private int totalPages;

    private List<T> items;
} 