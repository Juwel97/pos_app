package com.example.posapp.dto;

import com.example.posapp.entity.Item;

import java.util.List;

public record ItemPageResponse(
        List<Item> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasPrev,
        boolean hasNext
) {
}
