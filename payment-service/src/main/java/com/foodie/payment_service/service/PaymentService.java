package com.foodie.payment_service.service;

import com.foodie.payment_service.event.OrderCreatedEvent;
import com.foodie.payment_service.event.PaymentCompletedEvent;
import com.foodie.payment_service.model.Payment;
import com.foodie.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    private static final String TOPIC = "payment-completed";

    @Transactional
    public Payment processPayment(OrderCreatedEvent orderEvent) {
        Payment payment = Payment.builder()
                .orderUuid(orderEvent.getOrderUuid())
                .customerEmail(orderEvent.getCustomerEmail())
                .amount(orderEvent.getTotal())
                .method("UPI")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .paymentUuid(UUID.randomUUID().toString())
                .build();

        Payment saved = paymentRepository.save(payment);

        publishPaymentCompletedEvent(saved);
        log.info("Payment processed for order {} with status {}", saved.getOrderUuid(), saved.getStatus());
        return saved;
    }

    public Payment manualPayment(Payment payment) {
        payment.setCreatedAt(Instant.now());
        payment.setPaymentUuid(UUID.randomUUID().toString());
        if (payment.getStatus() == null) payment.setStatus("SUCCESS");

        Payment saved = paymentRepository.save(payment);
        publishPaymentCompletedEvent(saved);
        log.info("Manual payment processed for order {} with status {}", saved.getOrderUuid(), saved.getStatus());
        return saved;
    }

    private void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderUuid(payment.getOrderUuid())
                .customerEmail(payment.getCustomerEmail())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();

        kafkaTemplate.send(TOPIC, payment.getOrderUuid(), event);
    }

    public Payment getByPaymentUuid(String paymentUuid) {
        return paymentRepository.findByPaymentUuid(paymentUuid).orElse(null);
    }

    public List<Payment> getByOrderUuid(String orderUuid) {
        return paymentRepository.findByOrderUuid(orderUuid);
    }

    public Payment updatePayment(String paymentUuid, Payment paymentUpdate) {
        Payment existing = paymentRepository.findByPaymentUuid(paymentUuid).orElse(null);
        if (existing == null) return null;

        if (paymentUpdate.getStatus() != null) existing.setStatus(paymentUpdate.getStatus());
        if (paymentUpdate.getMethod() != null) existing.setMethod(paymentUpdate.getMethod());

        return paymentRepository.save(existing);
    }

    // FIXED: Changed return type from Payment to List<Payment>
    public List<Payment> getByCustomerEmail(String customerEmail) {
        return paymentRepository.findByCustomerEmail(customerEmail);
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
}