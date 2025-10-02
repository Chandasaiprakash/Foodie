package com.foodie.delivery_service.listener;


import com.foodie.delivery_service.event.PaymentCompletedEvent;
import com.foodie.delivery_service.model.Delivery;
import com.foodie.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = "payment-completed", groupId = "delivery-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handle(PaymentCompletedEvent event) {
        // Only assign on SUCCESS
        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            Delivery d = deliveryService.assignForOrder(event);
            // optionally log or expose metrics
            System.out.println("Assigned delivery: " + d.getId() + " for order " + d.getOrderUuid());
        } else {
            System.out.println("Payment not successful for order: " + event.getOrderUuid());
        }
    }
}

