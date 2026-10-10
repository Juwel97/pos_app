package com.example.posapp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_history")
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "total_offer_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOfferPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderHistoryItem> items = new ArrayList<>();

    protected OrderHistory() {
    }

    public OrderHistory(int discountPercent, int totalQuantity, BigDecimal totalPrice, BigDecimal totalOfferPrice) {
        this.createdAt = LocalDateTime.now();
        this.discountPercent = discountPercent;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.totalOfferPrice = totalOfferPrice;
    }

    public void addItem(OrderHistoryItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getDiscountPercent() { return discountPercent; }
    public int getTotalQuantity() { return totalQuantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BigDecimal getTotalOfferPrice() { return totalOfferPrice; }
    public List<OrderHistoryItem> getItems() { return items; }
}