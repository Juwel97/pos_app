package com.example.posapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CheckoutRequest(
        @NotEmpty List<@Valid CheckoutItemRequest> items,
        @Min(0) @Max(100) int discountPercent) {
}