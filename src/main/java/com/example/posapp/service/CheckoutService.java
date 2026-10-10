package com.example.posapp.service;

import com.example.posapp.dto.CheckoutItemRequest;
import com.example.posapp.dto.CheckoutRequest;
import com.example.posapp.dto.OrderItemResponse;
import com.example.posapp.dto.OrderResponse;
import com.example.posapp.entity.Item;
import com.example.posapp.entity.OrderHistory;
import com.example.posapp.entity.OrderHistoryItem;
import com.example.posapp.exception.InsufficientStockException;
import com.example.posapp.exception.ItemNotFoundException;
import com.example.posapp.repository.ItemRepository;
import com.example.posapp.repository.OrderHistoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final ItemRepository itemRepository;
    private final OrderHistoryRepository orderRepository;

    public CheckoutService(ItemRepository itemRepository, OrderHistoryRepository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CheckoutItemRequest item : request.items()) {
            long itemId = item.itemId();
            quantities.put(itemId, quantities.getOrDefault(itemId, 0) + item.quantity());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal offerTotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        List<OrderHistoryItem> orderItems = new ArrayList<>();
        BigDecimal discountMultiplier = BigDecimal.valueOf(100L - request.discountPercent());

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Item item = itemRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> new ItemNotFoundException(entry.getKey()));
            int quantity = entry.getValue();
            if (item.getQuantity() < quantity) {
                throw new InsufficientStockException(item.getName(), item.getQuantity());
            }

            BigDecimal unitPrice = item.getPrice();
            BigDecimal offerPrice = unitPrice.multiply(discountMultiplier)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            offerTotal = offerTotal.add(offerPrice.multiply(BigDecimal.valueOf(quantity)));
            totalQuantity += quantity;
            orderItems.add(new OrderHistoryItem(item.getId(), item.getName(), quantity, unitPrice, offerPrice));
            item.reduceQuantity(quantity);
        }

        OrderHistory order = new OrderHistory(request.discountPercent(), totalQuantity, subtotal, offerTotal);
        orderItems.forEach(order::addItem);
        OrderHistory savedOrder = orderRepository.saveAndFlush(order);
        List<OrderItemResponse> responseItems = orderItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getItemId(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getOfferPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                        item.getOfferPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getCreatedAt(),
                savedOrder.getDiscountPercent(),
                savedOrder.getTotalQuantity(),
                savedOrder.getTotalPrice(),
                savedOrder.getTotalOfferPrice(),
                responseItems);
    }
}