package com.foodie.order_service.listener;

import com.foodie.common.events.PaymentFailedEvent;
import com.foodie.order_service.model.Order;
import com.foodie.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedEventListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-failed", groupId = "order-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("❌ Received PaymentFailedEvent for order {}", event.getOrderUuid());

        orderRepository.findByOrderUuid(event.getOrderUuid())
                .ifPresent(order -> {
                    order.setPaymentStatus("FAILED");
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                    log.info("🚫 Order {} marked as CANCELLED due to payment failure", order.getOrderUuid());
                });
    }
}
