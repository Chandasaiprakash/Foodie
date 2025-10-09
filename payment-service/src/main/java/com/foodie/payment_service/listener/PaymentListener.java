package com.foodie.payment_service.listener;


import com.foodie.common.events.OrderCreatedEvent;
import com.foodie.payment_service.model.Payment;
import com.foodie.payment_service.repository.PaymentRepository;
import com.foodie.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentListener {
    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // process payment automatically for order
        paymentService.processPayment(event);
    }
}

