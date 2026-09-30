package com.ecommerce.payment.event;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentSagaConsumer {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentSagaConsumer(PaymentRepository paymentRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-saga-group")
    @Transactional
    public void handleOrderEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            String orderNumber = event.get("orderNumber").asText();

            if ("ORDER_CREATED".equals(eventType)) {
                BigDecimal amount = new BigDecimal(event.get("totalAmount").asText());
                String customerId = event.has("customerId") ? event.get("customerId").asText() : "CUST-DEFAULT";

                // Simulate payment validation (e.g. failure if amount > 10000)
                if (amount.compareTo(new BigDecimal("10000")) <= 0) {
                    Payment payment = new Payment("PAY-" + UUID.randomUUID().toString().substring(0, 8),
                            orderNumber, customerId, amount, "SUCCESS");
                    paymentRepository.save(payment);
                    publishEvent("PAYMENT_PROCESSED", orderNumber, payment.getPaymentId(), "SUCCESS");
                } else {
                    Payment payment = new Payment("PAY-" + UUID.randomUUID().toString().substring(0, 8),
                            orderNumber, customerId, amount, "FAILED");
                    paymentRepository.save(payment);
                    publishEvent("PAYMENT_FAILED", orderNumber, payment.getPaymentId(), "FAILED");
                }
            } else if ("ORDER_CANCELLED".equals(eventType)) {
                // Compensating Transaction: Process Refund if payment was charged
                paymentRepository.findByOrderNumber(orderNumber).ifPresent(payment -> {
                    if ("SUCCESS".equals(payment.getStatus())) {
                        payment.setStatus("REFUNDED");
                        paymentRepository.save(payment);
                        publishEvent("PAYMENT_REFUNDED", orderNumber, payment.getPaymentId(), "REFUNDED");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishEvent(String eventType, String orderNumber, String paymentId, String status) {
        String payload = String.format("{\"eventType\":\"%s\", \"orderNumber\":\"%s\", \"paymentId\":\"%s\", \"status\":\"%s\"}",
                eventType, orderNumber, paymentId, status);
        kafkaTemplate.send("payment-events", orderNumber, payload);
    }
}
