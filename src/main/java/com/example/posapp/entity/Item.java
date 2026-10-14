package com.example.posapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "mfg_name", length = 120)
    private String mfgName;

    @Column(name = "schedule_h1", nullable = false)
    private boolean scheduleH1;

    @Column(name = "sku", nullable = false, unique = true, length = 60)
    private String serialNumber;
    
    @Column(name = "batch_number", length = 60)
    private String batchNumber;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "capsules_per_strip")
    private Integer capsulesPerStrip;

    @Column(name = "capsule_quantity")
    private Integer capsuleQuantity;

    @Column(length = 80)
    private String category;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    protected Item() {
    }

    public Item(String name, String serialNumber, String batchNumber, String description, BigDecimal price,
            Integer quantity, String category, LocalDate expireDate) {
        this(name, serialNumber, batchNumber, description, price, quantity, category, expireDate, null);
    }

    public Item(String name, String serialNumber, String batchNumber, String description, BigDecimal price,
            Integer quantity, String category, LocalDate expireDate, String mfgName) {
        this(name, serialNumber, batchNumber, description, price, quantity, category, expireDate, mfgName, false);
    }

    public Item(String name, String serialNumber, String batchNumber, String description, BigDecimal price,
            Integer quantity, String category, LocalDate expireDate, String mfgName, boolean scheduleH1) {
        this.name = name;
        this.mfgName = mfgName;
        this.scheduleH1 = scheduleH1;
        this.serialNumber = serialNumber;
        this.batchNumber = batchNumber;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.expireDate = expireDate;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getMfgName() { return mfgName; }
    public boolean isScheduleH1() { return scheduleH1; }
    public String getSerialNumber() { return serialNumber; }
    public String getBatchNumber() { return batchNumber; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public Integer getCapsulesPerStrip() { return capsulesPerStrip; }
    public Integer getCapsuleQuantity() { return capsuleQuantity; }
    public Integer getLooseCapsules() {
        return isCapsuleTracked() ? capsuleQuantity % capsulesPerStrip : 0;
    }
    public boolean isCapsuleTracked() { return capsulesPerStrip != null && capsuleQuantity != null; }
    public String getCategory() { return category; }
    public LocalDate getExpireDate() { return expireDate; }

    public void reduceQuantity(int amount) {
        this.quantity -= amount;
    }

    public void reduceCapsuleQuantity(int amount) {
        this.capsuleQuantity -= amount;
        this.quantity = this.capsuleQuantity / this.capsulesPerStrip;
    }

    public void update(String name, String mfgName, String serialNumber, String batchNumber, String description, BigDecimal price,
            Integer quantity, String category, LocalDate expireDate, boolean scheduleH1) {
        this.name = name;
        this.mfgName = mfgName;
        this.scheduleH1 = scheduleH1;
        this.serialNumber = serialNumber;
        this.batchNumber = batchNumber;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.expireDate = expireDate;
    }

    public void configureCapsuleStock(Integer capsulesPerStrip, Integer capsuleQuantity) {
        this.capsulesPerStrip = capsulesPerStrip;
        this.capsuleQuantity = capsuleQuantity;
        this.quantity = capsuleQuantity == null ? this.quantity : capsuleQuantity / capsulesPerStrip;
    }
}