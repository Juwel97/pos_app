package com.example.posapp.controller;

import com.example.posapp.dto.CheckoutRequest;
import com.example.posapp.dto.OrderResponse;
import com.example.posapp.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final CheckoutService service;

    public OrderController(CheckoutService service) {
        this.service = service;
    }

    @PostMapping("/orders/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return service.checkout(request);
    }
}