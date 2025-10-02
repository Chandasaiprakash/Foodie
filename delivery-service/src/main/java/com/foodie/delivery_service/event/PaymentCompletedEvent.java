package com.foodie.delivery_service.event;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {
    private String orderUuid;
    private String customerEmail;
    private String customerPhone;
    private Double amount;
    private String status; // SUCCESS / FAILED
}

