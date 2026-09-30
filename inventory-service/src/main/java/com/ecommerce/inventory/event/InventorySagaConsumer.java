package com.ecommerce.inventory.event;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventorySagaConsumer {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InventorySagaConsumer(InventoryRepository inventoryRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-saga-group")
    @Transactional
    public void handleOrderEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            String orderNumber = event.get("orderNumber").asText();

            if ("ORDER_CREATED".equals(eventType)) {
                String productId = event.get("productId").asText();
                int quantity = event.get("quantity").asInt();

                Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
                if (inventory != null && inventory.getAvailableStock() >= quantity) {
                    // Deduct stock & reserve
                    inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
                    inventory.setReservedStock(inventory.getReservedStock() + quantity);
                    inventoryRepository.save(inventory);

                    publishEvent("INVENTORY_RESERVED", orderNumber, productId, quantity);
                } else {
                    publishEvent("INVENTORY_FAILED", orderNumber, productId, quantity);
                }
            } else if ("ORDER_CANCELLED".equals(eventType)) {
                // Compensating Transaction: Release reserved stock back if order failed in payment
                String productId = event.has("productId") ? event.get("productId").asText() : null;
                int quantity = event.has("quantity") ? event.get("quantity").asInt() : 0;

                if (productId != null) {
                    Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
                    if (inventory != null) {
                        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
                        inventory.setReservedStock(Math.max(0, inventory.getReservedStock() - quantity));
                        inventoryRepository.save(inventory);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishEvent(String eventType, String orderNumber, String productId, int quantity) {
        String payload = String.format("{\"eventType\":\"%s\", \"orderNumber\":\"%s\", \"productId\":\"%s\", \"quantity\":%d}",
                eventType, orderNumber, productId, quantity);
        kafkaTemplate.send("inventory-events", orderNumber, payload);
    }
}
