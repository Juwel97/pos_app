package com.example.posapp.service;

import com.example.posapp.repository.ItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalysisService {

    private final ItemRepository repository;

    public AnalysisService(ItemRepository repository) {
        this.repository = repository;
    }

    public long getTotalCount() {
        return repository.count();
    }

    public BigDecimal getTotalValue() {
        return repository.findAll().stream()
            .map(item -> item.isCapsuleTracked()
                ? item.getPrice().multiply(BigDecimal.valueOf(item.getCapsuleQuantity()))
                    .divide(BigDecimal.valueOf(item.getCapsulesPerStrip()), 2, RoundingMode.HALF_UP)
                : item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public long getLimitedStockCount() {
        return repository.countLimitedStock();
    }
}
