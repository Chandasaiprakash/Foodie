package com.foodie.order_service.service;


import com.foodie.order_service.model.Order;
import com.foodie.common.events.OrderCreatedEvent;
import com.foodie.order_service.model.OrderItem;
import com.foodie.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "order-created";

    // Create order
    @Transactional
    public Order createOrder(Order orderRequest, String customerEmail) {
        Order order = new Order();
        order.setCustomerEmail(customerEmail);
        order.setOrderUuid(UUID.randomUUID().toString());
        order.setStatus("CREATED");
        order.setPaymentStatus("PENDING"); // ✅ default payment status

        order.setCreatedAt(Instant.now());
        order.setRestaurantId(orderRequest.getRestaurantId());
        order.setRestaurantName(orderRequest.getRestaurantName());
        order.setCustomerPhone(orderRequest.getCustomerPhone());
        order.setItems(orderRequest.getItems());

        double total = orderRequest.getItems().stream()
                .mapToDouble(item -> item.getPrice() * (item.getQuantity() == null ? 1 : item.getQuantity()))
                .sum();
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        // Publish event to Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderUuid(saved.getOrderUuid())
                .customerEmail(saved.getCustomerEmail())
                .customerPhone(saved.getCustomerPhone())
                .restaurantId(saved.getRestaurantId())
                .restaurantName(saved.getRestaurantName())
                .items(saved.getItems().stream()
                        .map(i -> new OrderCreatedEvent.OrderItemDto(i.getName(), i.getQuantity(), i.getPrice()))
                        .collect(Collectors.toList()))
                .total(saved.getTotal())
                .build();

        kafkaTemplate.send(TOPIC, saved.getOrderUuid(), event);
        return saved;
    }

   /* // Fetch order by ID
    public Optional<Order> getById(Long id) {
        return orderRepository.findById(id);
    }

    // Fetch order by UUID
    public Optional<Order> getByUuid(String uuid) {
        return orderRepository.findByOrderUuid(uuid);
    }
*/
    // Fetch orders by customer email
    public List<Order> getByCustomerEmail(String customerEmail) {
        return orderRepository.findByCustomerEmail(customerEmail);
    }

    // Check ownership
    /*public boolean isOwnedBy(String orderUuid, String email) {
        return orderRepository.findByOrderUuid(orderUuid)
                .map(o -> o.getCustomerEmail().equalsIgnoreCase(email))
                .orElse(false);
    }*/
/*
    // Fetch order by ID with ownership validation
    public Optional<Order> getByIdIfOwned(Long id, String email) {
        return getById(id).filter(order -> order.getCustomerEmail().equalsIgnoreCase(email));
    }

    // Fetch order by UUID with ownership validation
    public Optional<Order> getByUuidIfOwned(String uuid, String email) {
        return getByUuid(uuid).filter(order -> order.getCustomerEmail().equalsIgnoreCase(email));
    }*/
}