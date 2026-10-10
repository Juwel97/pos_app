package com.example.posapp.service;

import com.example.posapp.repository.ItemRepository;
import java.math.BigDecimal;
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
        return repository.getTotalInventoryValue();
    }

    public long getLimitedStockCount() {
        return repository.countLimitedStock();
    }
}
