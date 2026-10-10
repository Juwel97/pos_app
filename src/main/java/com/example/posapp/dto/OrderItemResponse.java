package com.example.posapp.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long itemId,
        String itemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal offerPrice,
        BigDecimal lineTotal,
        BigDecimal lineOfferTotal) {
}