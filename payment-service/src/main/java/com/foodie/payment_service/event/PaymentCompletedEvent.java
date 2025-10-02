package com.foodie.payment_service.event;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {
    private String orderUuid;
    private String customerEmail;
    private Double amount;
    private String status;
}

