package com.example.posapp.controller;

import com.example.posapp.service.AnalysisService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @GetMapping("/total-count")
    public long getTotalCount() {
        return service.getTotalCount();
    }

    @GetMapping("/total-value")
    public BigDecimal getTotalValue() {
        return service.getTotalValue();
    }

    @GetMapping("/limited-stock")
    public long getLimitedStockCount() {
        return service.getLimitedStockCount();
    }
}
