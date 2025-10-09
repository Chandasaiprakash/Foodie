package com.foodie.notification_service.listener;

import com.foodie.common.events.OrderUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderUpdatedEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order-updated", groupId = "notification-service-group")
    public void handleOrderUpdated(OrderUpdatedEvent event) {
        log.info("📦 Order updated: {}", event);

        // ✅ Send it to the user via WebSocket
        String destination = "/topic/orders/" + event.getCustomerEmail();
        messagingTemplate.convertAndSend(destination, event);

        log.info("📡 Sent WebSocket update to {}", destination);
    }
}

