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
        Map<SaleLine, Integer> quantities = new LinkedHashMap<>();
        for (CheckoutItemRequest item : request.items()) {
            String saleUnit = item.saleUnit() == null ? "STRIP" : item.saleUnit().toUpperCase();
            if (!saleUnit.equals("STRIP") && !saleUnit.equals("CAPSULE")) {
                throw new IllegalArgumentException("Sale unit must be STRIP or CAPSULE");
            }
            SaleLine line = new SaleLine(item.itemId(), saleUnit);
            quantities.put(line, quantities.getOrDefault(line, 0) + item.quantity());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal offerTotal = BigDecimal.ZERO;
        BigDecimal cgstTotal = BigDecimal.ZERO;
        BigDecimal sgstTotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        List<OrderHistoryItem> orderItems = new ArrayList<>();
        BigDecimal discountMultiplier = BigDecimal.valueOf(100L - request.discountPercent());

        for (Map.Entry<SaleLine, Integer> entry : quantities.entrySet()) {
            Item item = itemRepository.findByIdForUpdate(entry.getKey().itemId())
                .orElseThrow(() -> new ItemNotFoundException(entry.getKey().itemId()));
            int quantity = entry.getValue();
            String saleUnit = entry.getKey().saleUnit();
            boolean capsuleSale = saleUnit.equals("CAPSULE");
            if (capsuleSale && !item.isCapsuleTracked()) {
                throw new IllegalArgumentException(item.getName() + " is not configured for capsule sales");
            }

            int stockToConsume = capsuleSale ? quantity : item.isCapsuleTracked()
                    ? Math.multiplyExact(quantity, item.getCapsulesPerStrip()) : quantity;
            int available = item.isCapsuleTracked() ? item.getCapsuleQuantity() : item.getQuantity();
            if (available < stockToConsume) {
                int availableDisplay = item.isCapsuleTracked() && !capsuleSale
                        ? available / item.getCapsulesPerStrip() : available;
                String unitLabel = item.isCapsuleTracked()
                        ? (capsuleSale ? "capsule(s)" : "strip(s)") : "unit(s)";
                throw new InsufficientStockException(item.getName(), availableDisplay, unitLabel);
            }

            BigDecimal unitPrice = capsuleSale
                    ? item.getPrice().divide(BigDecimal.valueOf(item.getCapsulesPerStrip()), 2, RoundingMode.HALF_UP)
                    : item.getPrice();
            BigDecimal offerPrice = unitPrice.multiply(discountMultiplier)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal lineOfferTotal = offerPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal lineCgst = taxAmount(lineOfferTotal);
            BigDecimal lineSgst = taxAmount(lineOfferTotal);
            subtotal = subtotal.add(lineTotal);
            offerTotal = offerTotal.add(lineOfferTotal);
            cgstTotal = cgstTotal.add(lineCgst);
            sgstTotal = sgstTotal.add(lineSgst);
            totalQuantity += quantity;
            int snapshotPackSize = item.isCapsuleTracked() ? item.getCapsulesPerStrip() : 1;
            orderItems.add(new OrderHistoryItem(item.getId(), item.getName(), item.getMfgName(), item.getBatchNumber(),
                    item.getExpireDate(), item.isScheduleH1(), quantity, unitPrice, offerPrice, saleUnit, snapshotPackSize));
            if (item.isCapsuleTracked()) item.reduceCapsuleQuantity(stockToConsume);
            else item.reduceQuantity(quantity);
        }

        OrderHistory order = new OrderHistory(
                request.discountPercent(),
                request.doctorName(),
                request.patientName(),
                request.patientPhone(),
                totalQuantity,
                subtotal,
                offerTotal);
        orderItems.forEach(order::addItem);
        OrderHistory savedOrder = orderRepository.saveAndFlush(order);
        List<OrderItemResponse> responseItems = orderItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getItemId(),
                        item.getItemName(),
                        item.getMfgName(),
                        item.getBatchNumber(),
                        item.getExpireDate(),
                        item.isScheduleH1(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getOfferPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                        item.getOfferPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                        taxAmount(item.getOfferPrice().multiply(BigDecimal.valueOf(item.getQuantity()))),
                        taxAmount(item.getOfferPrice().multiply(BigDecimal.valueOf(item.getQuantity()))),
                        item.getSaleUnit(),
                        item.getCapsulesPerStrip()))
                .toList();

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getCreatedAt(),
                savedOrder.getDiscountPercent(),
                savedOrder.getDoctorName(),
                savedOrder.getPatientName(),
                savedOrder.getPatientPhone(),
                savedOrder.getTotalQuantity(),
                savedOrder.getTotalPrice(),
                savedOrder.getTotalOfferPrice(),
                                cgstTotal,
                                sgstTotal,
                                offerTotal.add(cgstTotal).add(sgstTotal),
                responseItems);
    }

        private BigDecimal taxAmount(BigDecimal taxableAmount) {
                return taxableAmount.multiply(new BigDecimal("0.025")).setScale(2, RoundingMode.HALF_UP);
        }

    private record SaleLine(Long itemId, String saleUnit) { }
}