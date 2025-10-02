package com.foodie.delivery_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// If you run Order Service locally on 8082:
@FeignClient(name = "order-service", url = "${order.service.base-url:http://localhost:8082}")
public interface OrderServiceClient {

    // expects OrderController.getByUuid returning JSON with customerEmail/customerPhone
    @GetMapping("/orders/{orderUuid}")
    OrderDto getOrder(@PathVariable("orderUuid") String orderUuid);

    record OrderDto(String orderUuid, String customerEmail, String customerPhone) {}
}

