package com.ecommerce.order.controller;

import com.ecommerce.order.event.KafkaProducer;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final KafkaProducer kafkaProducer;

    public OrderController(OrderRepository orderRepository, KafkaProducer kafkaProducer) {
        this.orderRepository = orderRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping
    @CircuitBreaker(name = "inventoryServiceCB", fallbackMethod = "orderFallback")
    public ResponseEntity<Order> createOrder(@RequestBody Order orderRequest) {
        orderRequest.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        orderRequest.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(orderRequest);

        // Publish OrderCreatedEvent to Kafka to initiate Choreography Saga
        String eventPayload = String.format(
            "{\"eventType\":\"ORDER_CREATED\", \"orderNumber\":\"%s\", \"productId\":\"%s\", \"quantity\":%d, \"totalAmount\":%s, \"customerId\":\"%s\"}",
            savedOrder.getOrderNumber(), savedOrder.getProductId(), savedOrder.getQuantity(),
            savedOrder.getTotalAmount(), savedOrder.getCustomerId()
        );
        kafkaProducer.sendEvent("order-events", savedOrder.getOrderNumber(), eventPayload);

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<String> orderFallback(Order orderRequest, Throwable t) {
        return ResponseEntity.status(503)
                .body("Order service is currently undergoing high load or downstream services are unavailable. Please retry later. Detail: " + t.getMessage());
    }
}
