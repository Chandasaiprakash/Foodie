package com.foodie.payment_service.controller;

import com.foodie.payment_service.model.Payment;
import com.foodie.payment_service.service.PaymentService;
import com.foodie.payment_service.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

    @Value("${razorpay.key-id}")
    private String razorKey;

    // 🧾 Manual payment (for testing)
    @PostMapping
    public ResponseEntity<Payment> manualPay(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.manualPayment(payment));
    }

    // 🔍 Get all payments by customer
    @GetMapping("/customer/{customerEmail}")
    public ResponseEntity<List<Payment>> getByCustomerEmail(@PathVariable String customerEmail) {
        List<Payment> payments = paymentService.getByCustomerEmail(customerEmail);
        return payments.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(payments);
    }

    // 🛠️ Update a payment manually
    @PutMapping("/{paymentUuid}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable String paymentUuid,
            @RequestBody Payment paymentUpdate) {
        Payment updated = paymentService.updatePayment(paymentUuid, paymentUpdate);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    // 🔎 Get payment by order UUID
    @GetMapping("/order/{orderUuid}")
    public ResponseEntity<List<Payment>> getByOrder(@PathVariable String orderUuid) {
        List<Payment> payments = paymentService.getByOrderUuid(orderUuid);
        return payments.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(payments);
    }

    // 💳 Step 1: Create Razorpay order & pending record
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) throws Exception {
        String orderUuid = (String) body.get("orderUuid");
        double amount = Double.parseDouble(body.get("amount").toString());
        String customerEmail = (String) body.get("customerEmail");

        log.info("🧾 Creating Razorpay order for orderUuid={} amount={} email={}", orderUuid, amount, customerEmail);

        paymentService.createPendingForOrder(orderUuid, customerEmail, amount);
        JSONObject razorOrder = razorpayService.createOrder(orderUuid, amount);

        return ResponseEntity.ok(Map.of(
                "razorKey", razorKey,
                "orderId", razorOrder.getString("id"),
                "amount", razorOrder.getInt("amount"),
                "currency", razorOrder.getString("currency"),
                "receipt", orderUuid
        ));
    }

    // ✅ Step 2: Verify payment success
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        try {
            String orderUuid = body.get("receipt") != null ? body.get("receipt") : body.get("orderUuid");
            if (orderUuid == null)
                return ResponseEntity.badRequest().body(Map.of("error", "orderUuid missing"));

            log.info("✅ Verifying successful payment for order {}", orderUuid);
            paymentService.markSuccess(orderUuid);

            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Payment verified successfully"));
        } catch (Exception e) {
            log.error("❌ Error verifying payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verification failed", "details", e.getMessage()));
        }
    }

    // ❌ Step 3: Handle payment failures (cancelled or failed)
    @PostMapping("/fail")
    public ResponseEntity<?> markFailed(@RequestBody Map<String, String> body) {
        try {
            String orderUuid = body.get("orderUuid");
            String reason = body.getOrDefault("reason", "Unknown failure");
            if (orderUuid == null)
                return ResponseEntity.badRequest().body(Map.of("error", "orderUuid missing"));

            log.warn("❌ Marking payment as failed for order {} - Reason: {}", orderUuid, reason);
            paymentService.markFailed(orderUuid, reason);

            return ResponseEntity.ok(Map.of("status", "FAILED", "message", reason));
        } catch (Exception e) {
            log.error("❌ Error marking payment failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment failure update failed", "details", e.getMessage()));
        }
    }

    // 🌐 Step 4: Razorpay Webhook (optional but recommended)
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("📩 Received Razorpay Webhook: {}", payload);

            String event = (String) payload.get("event");
            Map<String, Object> payloadData = (Map<String, Object>) payload.get("payload");
            Map<String, Object> orderObj = (Map<String, Object>) payloadData.get("order");
            Map<String, Object> orderData = (Map<String, Object>) orderObj.get("entity");

            String orderUuid = (String) orderData.get("receipt");
            if (orderUuid == null)
                return ResponseEntity.badRequest().body(Map.of("error", "orderUuid missing from webhook"));

            if ("payment.captured".equals(event)) {
                paymentService.markSuccess(orderUuid);
                log.info("✅ Webhook: Payment captured for order {}", orderUuid);
            } else if ("payment.failed".equals(event)) {
                paymentService.markFailed(orderUuid, "Payment failed via webhook");
                log.warn("❌ Webhook: Payment failed for order {}", orderUuid);
            } else {
                log.info("ℹ️ Webhook event ignored: {}", event);
            }

            return ResponseEntity.ok(Map.of("status", "processed"));
        } catch (Exception e) {
            log.error("❌ Error handling webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Webhook processing failed", "details", e.getMessage()));
        }
    }
}
