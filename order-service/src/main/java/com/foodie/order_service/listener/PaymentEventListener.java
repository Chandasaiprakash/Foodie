package com.foodie.order_service.listener;

import com.foodie.common.events.PaymentCompletedEvent;
import com.foodie.common.events.PaymentFailedEvent;
import com.foodie.common.events.OrderUpdatedEvent;
import com.foodie.order_service.model.Order;
import com.foodie.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.foodie.order_service.service.OrderService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-completed", groupId = "order-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("💰 Received PaymentCompletedEvent: {}", event);

        orderRepository.findByOrderUuid(event.getOrderUuid())
                .ifPresentOrElse(order -> {
                    order.setPaymentStatus(event.getStatus());
                    order.setStatus("CONFIRMED"); // ✅ Update order status
                    orderRepository.save(order);
                    log.info("✅ Order updated: {} with status CONFIRMED", order.getOrderUuid());

                    // ✅ Publish OrderUpdatedEvent so notification-service & frontend know
                    OrderUpdatedEvent updatedEvent = new OrderUpdatedEvent(
                            order.getOrderUuid(),
                            order.getStatus(),
                            order.getPaymentStatus(),
                            order.getCustomerEmail()
                    );

                    kafkaTemplate.send("order-updated", updatedEvent);
                    log.info("📢 Sent OrderUpdatedEvent: {}", updatedEvent);
                }, () -> log.warn("⚠️ No order found for UUID: {}", event.getOrderUuid()));
    }

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.warn("❌ PaymentFailedEvent received for order {}: {}", event.getOrderUuid(), event.getReason());
        orderService.updatePaymentStatus(event.getOrderUuid(), "FAILED");

    }

}
