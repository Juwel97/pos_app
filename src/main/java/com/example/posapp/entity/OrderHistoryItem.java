package com.example.posapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_history_items")
public class OrderHistoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_history_id", nullable = false)
    private OrderHistory order;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false, length = 120)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "offer_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal offerPrice;

    protected OrderHistoryItem() {
    }

    public OrderHistoryItem(Long itemId, String itemName, int quantity, BigDecimal unitPrice,
            BigDecimal offerPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.offerPrice = offerPrice;
    }

    void setOrder(OrderHistory order) { this.order = order; }

    public Long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getOfferPrice() { return offerPrice; }
}