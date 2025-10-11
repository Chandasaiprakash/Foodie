package com.foodie.order_service.controller;

import com.foodie.order_service.model.Order;
import com.foodie.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    // Create order
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestBody Order order,
            @RequestHeader("X-User-Email") String customerEmail) {
        /*log.info("customerEmailfromcontroller:{}", customerEmail);
        log.info("order.customerEmail:{}", order.getCustomerEmail());
        log.info("Order received from controller: {}", order);*/
        Order savedOrder = orderService.createOrder(order, customerEmail);
        return ResponseEntity.ok(savedOrder);
    }



    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String customerEmail) {

        return orderService.getByIdIfOwned(id, customerEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build()); // 403 if not owned
    }


    @GetMapping("/{orderUuid}")
    public ResponseEntity<Order> getByUuid(
            @PathVariable String orderUuid,
            @RequestHeader("X-User-Email") String customerEmail) {

        return orderService.getByUuidIfOwned(orderUuid, customerEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());


    // Get all orders of a customer
    @GetMapping("customer/{email}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable String email,@RequestHeader("X-User-Email") String customerEmail) {
        if (!email.equalsIgnoreCase(customerEmail)) {
            return ResponseEntity.status(403).build(); // forbid access
        }
        List<Order> list = orderService.getByCustomerEmail(email);
        return ResponseEntity.ok(list);
    }

  /*  @DeleteMapping("/{orderUuid}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderUuid) {
        orderService.deleteByOrderUuid(orderUuid);
        return ResponseEntity.noContent().build();
    }*/


   /* // Ownership validation for external services or WebSocket
    @GetMapping("/{orderUuid}/validate")
    public ResponseEntity<Boolean> validateOwnership(
            @PathVariable String orderUuid,
            @RequestParam String email) {

        boolean ok = orderService.isOwnedBy(orderUuid, email);
        return ResponseEntity.ok(ok);
    }*/
}
