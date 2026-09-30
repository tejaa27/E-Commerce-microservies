package com.ecommerce.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String id;
    private String orderNumber;
    private String customerId;
    private String eventType;
    private String message;
    private LocalDateTime timestamp;

    public NotificationLog() {}

    public NotificationLog(String orderNumber, String customerId, String eventType, String message) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.eventType = eventType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerId() { return customerId; }
    public String getEventType() { return eventType; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
