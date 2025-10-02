package com.foodie.delivery_service.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("deliveries")
public class Delivery {
    @Id
    private String id;
    private String orderUuid;
    private String partnerId;
    private String deliveryPersonEmail;
    private String status; // ASSIGNED, PICKED_UP, ON_THE_WAY, DELIVERED
    private Instant assignedAt;
    private Instant updatedAt;
    private String customerEmail;
    private String customerPhone;
}

