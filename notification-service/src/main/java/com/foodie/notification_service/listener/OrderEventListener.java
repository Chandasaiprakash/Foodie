package com.foodie.notification_service.listener;

import com.foodie.common.events.DeliveryEvent;
import com.foodie.common.events.OrderUpdatedEvent;
import com.foodie.notification_service.service.NotificationService; // Import NotificationService
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    // ✅ FIX: Inject your NotificationService
    private final NotificationService notificationService;

    @KafkaListener(topics = "order-updated", groupId = "notification-service-group",containerFactory = "orderUpdatedEventListenerFactory")
    public void handleOrderUpdatedEvent(OrderUpdatedEvent event) {
        log.info("Received OrderUpdatedEvent: {}", event);
        notificationService.broadcast(event);
    }

    @KafkaListener(topics = "delivery-events", groupId = "notification-service-group")
    public void handleDeliveryEvent(DeliveryEvent event) {
        log.info("Received DeliveryEvent: {}", event);
        notificationService.broadcast(event);
    }

}