package com.foodie.payment_service.service;

import com.foodie.common.events.OrderCreatedEvent;
import com.foodie.common.events.PaymentCompletedEvent;
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

    /**
     * Called when OrderCreatedEvent is received.
     * We only create a payment entry in PENDING state here.
     */
    @Transactional
    public Payment processPayment(OrderCreatedEvent orderEvent) {
        Payment payment = Payment.builder()
                .orderUuid(orderEvent.getOrderUuid())
                .customerEmail(orderEvent.getCustomerEmail())
                .amount(orderEvent.getTotal())
                .method("UPI")
                .status("PENDING") // ✅ only pending now
                .createdAt(Instant.now())
                .paymentUuid(UUID.randomUUID().toString())
                .build();

        Payment saved = paymentRepository.save(payment);

        log.info("💰 Payment record created for order {} with status {}", saved.getOrderUuid(), saved.getStatus());
        return saved;
    }

    /**
     * Called when user clicks “Pay Now” in frontend.
     * Marks payment as SUCCESS and publishes PaymentCompletedEvent.
     */
    @Transactional
    public Payment completePayment(String orderUuid) {
        Payment payment = paymentRepository.findByOrderUuid(orderUuid)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderUuid));

        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        publishPaymentCompletedEvent(payment);
        log.info("✅ Payment completed for order {} by {}", payment.getOrderUuid(), payment.getCustomerEmail());
        return payment;
    }

    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderUuid(payment.getOrderUuid())
                .customerEmail(payment.getCustomerEmail())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();

        kafkaTemplate.send(TOPIC, payment.getOrderUuid(), event);
        log.info("📢 Published PaymentCompletedEvent: {}", event);
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

    public List<Payment> getByCustomerEmail(String customerEmail) {
        return paymentRepository.findByCustomerEmail(customerEmail);
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id).orElse(null);
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
}
