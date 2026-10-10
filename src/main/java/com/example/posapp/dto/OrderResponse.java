package com.example.posapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        LocalDateTime createdAt,
        int discountPercent,
        int totalQuantity,
        BigDecimal totalPrice,
        BigDecimal totalOfferPrice,
        List<OrderItemResponse> items) {
}