package com.foodie.payment_service.service;

import com.foodie.common.events.OrderCreatedEvent;
import com.foodie.common.events.PaymentCompletedEvent;
import com.foodie.common.events.PaymentFailedEvent;
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

    // ✅ FIX: Make KafkaTemplate generic to handle multiple event types
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_PAYMENT_SUCCESS = "payment-completed";
    private static final String TOPIC_PAYMENT_FAILED = "payment-failed";

    /**
     * 🧾 Called when an OrderCreatedEvent is received.
     * This just creates a "PENDING" payment record for tracking.
     */
    @Transactional
    public Payment processPayment(OrderCreatedEvent orderEvent) {
        Payment payment = Payment.builder()
                .orderUuid(orderEvent.getOrderUuid())
                .customerEmail(orderEvent.getCustomerEmail())
                .amount(orderEvent.getTotal())
                .method("ONLINE")
                .status("PENDING")
                .createdAt(Instant.now())
                .paymentUuid(UUID.randomUUID().toString())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("💰 Payment record created for order {} with status {}", saved.getOrderUuid(), saved.getStatus());
        return saved;
    }

    /**
     * 💳 Marks payment as SUCCESS when user completes payment
     * and publishes a PaymentCompletedEvent to Kafka.
     */
    @Transactional
    public Payment markSuccess(String orderUuid) {
        List<Payment> list = paymentRepository.findByOrderUuid(orderUuid);
        if (list.isEmpty()) throw new RuntimeException("Payment not found for order: " + orderUuid);

        Payment payment = list.get(0);
        payment.setStatus("SUCCESS");
        Payment saved = paymentRepository.save(payment);

        publishPaymentCompletedEvent(saved);
        log.info("✅ Payment completed for order {} by {}", saved.getOrderUuid(), saved.getCustomerEmail());
        return saved;
    }

    /**
     * ❌ Marks payment as FAILED and publishes a PaymentFailedEvent.
     */
    @Transactional
    public Payment markFailed(String orderUuid, String reason) {
        List<Payment> list = paymentRepository.findByOrderUuid(orderUuid);
        if (list.isEmpty()) throw new RuntimeException("Payment not found for order: " + orderUuid);

        Payment payment = list.get(0);
        payment.setStatus("FAILED");
        Payment saved = paymentRepository.save(payment);

        publishPaymentFailedEvent(saved, reason);
        log.warn("❌ Payment failed for order {} - Reason: {}", orderUuid, reason);
        return saved;
    }

    /**
     * 📤 Publishes successful payment event to Kafka.
     */
    private void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderUuid(payment.getOrderUuid())
                .customerEmail(payment.getCustomerEmail())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();

        kafkaTemplate.send(TOPIC_PAYMENT_SUCCESS, payment.getOrderUuid(), event);
        log.info("📢 Published PaymentCompletedEvent → {}", event);
    }

    /**
     * 📤 Publishes failed payment event to Kafka.
     */
    private void publishPaymentFailedEvent(Payment payment, String reason) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderUuid(payment.getOrderUuid())
                .customerEmail(payment.getCustomerEmail())
                .amount(payment.getAmount())
                .reason(reason)
                .build();

        kafkaTemplate.send(TOPIC_PAYMENT_FAILED, payment.getOrderUuid(), event);
        log.warn("📢 Published PaymentFailedEvent → {}", event);
    }

    /**
     * 🔧 Helper for manual payments (testing purpose)
     */
    public Payment manualPayment(Payment payment) {
        payment.setCreatedAt(Instant.now());
        payment.setPaymentUuid(UUID.randomUUID().toString());
        if (payment.getStatus() == null) payment.setStatus("SUCCESS");
        Payment saved = paymentRepository.save(payment);

        publishPaymentCompletedEvent(saved);
        log.info("🧾 Manual payment processed for order {} with status {}", saved.getOrderUuid(), saved.getStatus());
        return saved;
    }

    /**
     * 🕓 When order is created, pre-create a pending payment.
     */
    @Transactional
    public Payment createPendingForOrder(String orderUuid, String customerEmail, double amount) {
        Payment payment = Payment.builder()
                .orderUuid(orderUuid)
                .customerEmail(customerEmail)
                .amount(amount)
                .method("ONLINE")
                .status("PENDING")
                .createdAt(Instant.now())
                .paymentUuid(UUID.randomUUID().toString())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("⏳ Created pending payment for order {}", orderUuid);
        return saved;
    }

    // 🔍 Utility methods
    public Payment getByPaymentUuid(String paymentUuid) {
        return paymentRepository.findByPaymentUuid(paymentUuid).orElse(null);
    }

    public List<Payment> getByOrderUuid(String orderUuid) {
        return paymentRepository.findByOrderUuid(orderUuid);
    }

    public List<Payment> getByCustomerEmail(String customerEmail) {
        return paymentRepository.findByCustomerEmail(customerEmail);
    }

    public Payment updatePayment(String paymentUuid, Payment paymentUpdate) {
        Payment existing = paymentRepository.findByPaymentUuid(paymentUuid).orElse(null);
        if (existing == null) return null;

        if (paymentUpdate.getStatus() != null) existing.setStatus(paymentUpdate.getStatus());
        if (paymentUpdate.getMethod() != null) existing.setMethod(paymentUpdate.getMethod());

        return paymentRepository.save(existing);
    }
}
