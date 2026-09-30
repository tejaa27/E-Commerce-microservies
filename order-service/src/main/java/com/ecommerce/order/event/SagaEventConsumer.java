package com.ecommerce.order.event;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SagaEventConsumer {

    private final OrderRepository orderRepository;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    public SagaEventConsumer(OrderRepository orderRepository, KafkaProducer kafkaProducer, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-saga-group")
    @Transactional
    public void consumeInventoryEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            String orderNumber = event.get("orderNumber").asText();

            Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
            if (order == null || order.getStatus() != OrderStatus.PENDING) return;

            if ("INVENTORY_RESERVED".equals(eventType)) {
                order.setInventoryReserved(true);
                evaluateSagaCompletion(order);
            } else if ("INVENTORY_FAILED".equals(eventType)) {
                cancelOrder(order, "INVENTORY_FAILURE");
            }
            orderRepository.save(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-saga-group")
    @Transactional
    public void consumePaymentEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            String orderNumber = event.get("orderNumber").asText();

            Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
            if (order == null || order.getStatus() != OrderStatus.PENDING) return;

            if ("PAYMENT_PROCESSED".equals(eventType)) {
                order.setPaymentProcessed(true);
                evaluateSagaCompletion(order);
            } else if ("PAYMENT_FAILED".equals(eventType)) {
                cancelOrder(order, "PAYMENT_FAILURE");
            }
            orderRepository.save(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void evaluateSagaCompletion(Order order) {
        if (Boolean.TRUE.equals(order.getInventoryReserved()) && Boolean.TRUE.equals(order.getPaymentProcessed())) {
            order.setStatus(OrderStatus.CONFIRMED);
            String payload = String.format("{\"eventType\":\"ORDER_COMPLETED\", \"orderNumber\":\"%s\", \"customerId\":\"%s\"}",
                    order.getOrderNumber(), order.getCustomerId());
            kafkaProducer.sendEvent("order-events", order.getOrderNumber(), payload);
        }
    }

    private void cancelOrder(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELLED);
        String cancelPayload = String.format("{\"eventType\":\"ORDER_CANCELLED\", \"orderNumber\":\"%s\", \"reason\":\"%s\"}",
                order.getOrderNumber(), reason);
        // Triggers compensating transactions in Inventory & Payment services if needed
        kafkaProducer.sendEvent("order-events", order.getOrderNumber(), cancelPayload);
    }
}
