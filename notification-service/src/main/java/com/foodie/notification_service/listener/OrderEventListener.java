package com.foodie.notification_service.listener;

import com.foodie.common.events.DeliveryEvent;
import com.foodie.common.events.OrderUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // This single listener handles all messages from the "order-updated" topic
    @KafkaListener(topics = "order-updated", groupId = "notification-service-group")
    public void handleOrderEvents(Object event) {
        log.info("Received event on order-updated topic: {}", event.getClass().getSimpleName());

        // Default values
        String customerEmail = null;
        Object payload = event;

        // ✅ Check the type of the incoming event
        if (event instanceof OrderUpdatedEvent) {
            OrderUpdatedEvent orderEvent = (OrderUpdatedEvent) event;
            customerEmail = orderEvent.getCustomerEmail();
            log.info("📦 Order updated: {}", orderEvent);
        } else if (event instanceof DeliveryEvent) {
            DeliveryEvent deliveryEvent = (DeliveryEvent) event;
            customerEmail = deliveryEvent.getCustomerEmail();
            log.info("🚚 Delivery update: {}", deliveryEvent);
        } else {
            log.warn("Received unknown event type: {}", event.getClass().getName());
            return; // Do nothing if the event type is unknown
        }

        if (customerEmail != null) {
            // Send the event payload to the user via WebSocket
            String destination = "/topic/orders/" + customerEmail;
            messagingTemplate.convertAndSend(destination, payload);
            log.info("📡 Sent WebSocket update to {}", destination);
        }
    }
}