package com.foodie.order_service.controller;

import com.foodie.order_service.model.Order;
import com.foodie.order_service.repository.OrderRepository;
import com.foodie.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final OrderRepository orderRepository;

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
    }

    // Get all orders of a customer
    @GetMapping("customer/{email}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable String email,@RequestHeader("X-User-Email") String customerEmail) {
        if (!email.equalsIgnoreCase(customerEmail)) {
            return ResponseEntity.status(403).build(); // forbid access
        }
        List<Order> list = orderService.getByCustomerEmail(email);
        return ResponseEntity.ok(list);
    }

        @PutMapping("/{orderUuid}/payment-status")
        public ResponseEntity<String> updatePaymentStatus(@PathVariable String orderUuid,
                @RequestParam String status) {
            Order order = orderRepository.findByOrderUuid(orderUuid)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setPaymentStatus(status);
            orderRepository.save(order);

            return ResponseEntity.ok("Payment status updated to " + status);
        }


    @DeleteMapping("/{orderUuid}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderUuid) {
        boolean deleted = orderService.deleteOrder(orderUuid);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Order deleted successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<Order> reorder(@RequestBody Order originalOrder) {
        Order newOrder = new Order();
        newOrder.setOrderUuid(UUID.randomUUID().toString());
        newOrder.setCustomerEmail(originalOrder.getCustomerEmail());
        newOrder.setRestaurantName(originalOrder.getRestaurantName());
        newOrder.setItems(originalOrder.getItems());
        newOrder.setTotal(originalOrder.getTotal());
        newOrder.setStatus("CREATED");
        newOrder.setPaymentStatus("PENDING");
        newOrder.setCreatedAt(Instant.now());

        Order saved = orderRepository.save(newOrder);
        log.info("🔁 Reordered: {}", saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/customer/{email}/paged")
    public ResponseEntity<Map<String, Object>> getPagedOrders(
            @PathVariable String email,
            @RequestHeader("X-User-Email") String customerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String search
    ) {
        if (!email.equalsIgnoreCase(customerEmail)) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> result = orderService.getPagedOrders(email, page, size, sort, search);
        return ResponseEntity.ok(result);
    }





   /* // Ownership validation for external services or WebSocket
    @GetMapping("/{orderUuid}/validate")
    public ResponseEntity<Boolean> validateOwnership(
            @PathVariable String orderUuid,
            @RequestParam String email) {

        boolean ok = orderService.isOwnedBy(orderUuid, email);
        return ResponseEntity.ok(ok);
    }*/
}
