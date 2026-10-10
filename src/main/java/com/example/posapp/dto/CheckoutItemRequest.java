package com.example.posapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutItemRequest(
        @NotNull @Positive Long itemId,
        @Positive int quantity) {
}