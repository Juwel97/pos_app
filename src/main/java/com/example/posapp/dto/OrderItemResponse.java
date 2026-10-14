package com.example.posapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderItemResponse(
        Long itemId,
        String itemName,
        String mfgName,
        String batchNumber,
        LocalDate expireDate,
        boolean scheduleH1,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal offerPrice,
        BigDecimal lineTotal,
        BigDecimal lineOfferTotal,
        BigDecimal lineCgst,
        BigDecimal lineSgst,
        String saleUnit,
        int capsulesPerStrip) {
}