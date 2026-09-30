package com.ecommerce.notification.event;

import com.ecommerce.notification.model.NotificationLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventListener {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void listenOrderEvents(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            String orderNumber = event.get("orderNumber").asText();

            String notificationMsg;
            if ("ORDER_COMPLETED".equals(eventType)) {
                notificationMsg = "Your order " + orderNumber + " has been successfully confirmed and processed!";
            } else if ("ORDER_CANCELLED".equals(eventType)) {
                notificationMsg = "Alert: Your order " + orderNumber + " was cancelled due to processing error.";
            } else {
                return;
            }

            NotificationLog log = new NotificationLog(orderNumber, "CUST-DEFAULT", eventType, notificationMsg);
            mongoTemplate.save(log);

            System.out.println(">>> [NOTIFICATION SENT]: " + notificationMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
