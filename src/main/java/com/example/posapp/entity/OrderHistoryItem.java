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
import java.time.LocalDate;

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

    @Column(name = "mfg_name", length = 120)
    private String mfgName;

    @Column(name = "batch_number", length = 60)
    private String batchNumber;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "schedule_h1", nullable = false)
    private boolean scheduleH1;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "offer_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal offerPrice;

    @Column(name = "sale_unit", nullable = false, length = 16)
    private String saleUnit = "STRIP";

    @Column(name = "capsules_per_strip", nullable = false)
    private int capsulesPerStrip = 1;

    protected OrderHistoryItem() {
    }

    public OrderHistoryItem(Long itemId, String itemName, String mfgName, String batchNumber, LocalDate expireDate,
            boolean scheduleH1, int quantity, BigDecimal unitPrice, BigDecimal offerPrice, String saleUnit,
            int capsulesPerStrip) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.mfgName = mfgName;
        this.batchNumber = batchNumber;
        this.expireDate = expireDate;
        this.scheduleH1 = scheduleH1;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.offerPrice = offerPrice;
        this.saleUnit = saleUnit;
        this.capsulesPerStrip = capsulesPerStrip;
    }

    void setOrder(OrderHistory order) { this.order = order; }

    public Long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getMfgName() { return mfgName; }
    public String getBatchNumber() { return batchNumber; }
    public LocalDate getExpireDate() { return expireDate; }
    public boolean isScheduleH1() { return scheduleH1; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getOfferPrice() { return offerPrice; }
    public String getSaleUnit() { return saleUnit; }
    public int getCapsulesPerStrip() { return capsulesPerStrip; }
}