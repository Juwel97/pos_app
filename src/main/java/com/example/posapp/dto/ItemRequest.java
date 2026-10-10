package com.example.posapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 60) String serialNumber,
        @Size(max = 60) String batchNumber,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @PositiveOrZero Integer quantity,
        @Size(max = 80) String category,
        LocalDate expireDate) {
}