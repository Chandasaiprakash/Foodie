package com.foodie.order_service.controller;

import com.foodie.order_service.model.Order;
import com.foodie.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Create order
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestBody Order order,
            @RequestHeader("X-User-Email") String customerEmail) {

        Order savedOrder = orderService.createOrder(order, customerEmail);
        return ResponseEntity.ok(savedOrder);
    }

    // Get order by DB id with ownership validation
    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String customerEmail) {

        return orderService.getByIdIfOwned(id, customerEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build()); // 403 if not owned
    }

    // Get order by UUID with ownership validation
    @GetMapping("/{orderUuid}")
    public ResponseEntity<Order> getByUuid(
            @PathVariable String orderUuid,
            @RequestHeader("X-User-Email") String customerEmail) {

        return orderService.getByUuidIfOwned(orderUuid, customerEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    // Get all orders of a customer
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable String email,@RequestHeader("X-User-Email") String customerEmail) {
        if (!email.equalsIgnoreCase(customerEmail)) {
            return ResponseEntity.status(403).build(); // forbid access
        }
        List<Order> list = orderService.getByCustomerEmail(email);
        return ResponseEntity.ok(list);
    }

    // Ownership validation for external services or WebSocket
    @GetMapping("/{orderUuid}/validate")
    public ResponseEntity<Boolean> validateOwnership(
            @PathVariable String orderUuid,
            @RequestParam String email) {

        boolean ok = orderService.isOwnedBy(orderUuid, email);
        return ResponseEntity.ok(ok);
    }
}
