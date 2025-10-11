package com.foodie.payment_service.controller;

import com.foodie.payment_service.model.Payment;
import com.foodie.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 🧾 Create manual payment (admin/testing)
    @PostMapping
    public ResponseEntity<Payment> manualPay(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.manualPayment(payment));
    }

    // 👤 Get payments for a customer
    @GetMapping("/customer/{customerEmail}")
    public ResponseEntity<List<Payment>> getByCustomerEmail(@PathVariable String customerEmail) {
        List<Payment> payments = paymentService.getByCustomerEmail(customerEmail);
        if (payments.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(payments);
    }

    // 💳 Update payment (admin/testing)
    @PutMapping("/{paymentUuid}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable String paymentUuid,
            @RequestBody Payment paymentUpdate) {

        Payment updatedPayment = paymentService.updatePayment(paymentUuid, paymentUpdate);
        if (updatedPayment == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updatedPayment);
    }

    // 🆔 Get payment by numeric ID
    @GetMapping("/id/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Long id) {
        Payment p = paymentService.getById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // 🔑 Get payment by UUID
    @GetMapping("/uuid/{paymentUuid}")
    public ResponseEntity<Payment> getByPaymentUuid(@PathVariable String paymentUuid) {
        Payment p = paymentService.getByPaymentUuid(paymentUuid);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }


    // 🧾 Get payments for a specific order
    @GetMapping("/order/{orderUuid}")
    public ResponseEntity<List<Payment>> getByOrder(@PathVariable String orderUuid) {
        List<Payment> payments = paymentService.getByOrderUuid(orderUuid);
        if (payments.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(payments);
    }


    // 💸 Endpoint used by frontend when user clicks "Pay Now"
    @PostMapping("/pay/{orderUuid}")
    public ResponseEntity<Payment> payNow(@PathVariable String orderUuid) {
        Payment updated = paymentService.completePayment(orderUuid);
        return ResponseEntity.ok(updated);
    }
}
