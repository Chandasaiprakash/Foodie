package com.foodie.order_service.listener;

import com.foodie.common.events.DeliveryEvent;
import com.foodie.common.events.OrderUpdatedEvent;
import com.foodie.order_service.model.Order;
import com.foodie.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventListener {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "delivery-events", groupId = "order-service-group")
    public void handleDeliveryEvent(DeliveryEvent event) {
        log.info("🚚 Received DeliveryEvent: {}", event);

        orderRepository.findByOrderUuid(event.getOrderUuid()).ifPresent(order -> {
            order.setStatus(event.getStatus());
            orderRepository.save(order);

            // ✅ publish OrderUpdatedEvent to notify frontend
            OrderUpdatedEvent updatedEvent = OrderUpdatedEvent.builder()
                    .orderUuid(order.getOrderUuid())
                    .status(order.getStatus())
                    .paymentStatus(order.getPaymentStatus())
                    .customerEmail(order.getCustomerEmail())
                    .build();

            kafkaTemplate.send("order-updated", updatedEvent);
            log.info("📤 Published OrderUpdatedEvent after delivery update: {}", updatedEvent);
        });
    }
}
