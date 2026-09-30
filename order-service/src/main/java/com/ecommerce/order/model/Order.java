package com.ecommerce.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Boolean inventoryReserved = false;
    private Boolean paymentProcessed = false;

    private LocalDateTime createdAt;

    public Order() {}

    public Order(String orderNumber, String customerId, String productId, Integer quantity, BigDecimal totalAmount, OrderStatus status) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Boolean getInventoryReserved() { return inventoryReserved; }
    public void setInventoryReserved(Boolean inventoryReserved) { this.inventoryReserved = inventoryReserved; }

    public Boolean getPaymentProcessed() { return paymentProcessed; }
    public void setPaymentProcessed(Boolean paymentProcessed) { this.paymentProcessed = paymentProcessed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
