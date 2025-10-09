package com.foodie.notification_service.listener;


import com.foodie.common.events.DeliveryEvent;
import com.foodie.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "delivery-events", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(DeliveryEvent event) {
        notificationService.broadcast(event);
    }
}

