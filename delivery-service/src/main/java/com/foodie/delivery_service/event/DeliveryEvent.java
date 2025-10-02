package com.foodie.delivery_service.event;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEvent {
    private String orderUuid;
    private String status; // ASSIGNED, PICKED_UP, ON_THE_WAY, DELIVERED
    private String deliveryPersonEmail;
    private String partnerId;
    private String customerEmail;
    private String customerPhone;
    private long timestamp;
}

