package com.foodie.common.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
    private String orderUuid;
    private String customerEmail;
    private String customerPhone;
    private String restaurantId;
    private String restaurantName;
    private List<OrderItemDto> items;
    private Double total;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String name;
        private Integer quantity;
        private Double price;
    }
}
