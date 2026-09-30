package com.ecommerce.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentId;
    private String orderNumber;
    private String customerId;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED, REFUNDED
    private LocalDateTime timestamp;

    public Payment() {}

    public Payment(String paymentId, String orderNumber, String customerId, BigDecimal amount, String status) {
        this.paymentId = paymentId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
