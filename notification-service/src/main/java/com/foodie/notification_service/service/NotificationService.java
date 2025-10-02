package com.foodie.notification_service.service;


import com.foodie.notification_service.event.DeliveryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(DeliveryEvent event) {
        // send to websocket topic
        messagingTemplate.convertAndSend("/topic/updates", event);

        // simulate email/sms by logging
        System.out.printf("[NOTIFICATION] Order %s status %s (customer %s)\n",
                event.getOrderUuid(), event.getStatus(), event.getCustomerEmail());
    }
}

