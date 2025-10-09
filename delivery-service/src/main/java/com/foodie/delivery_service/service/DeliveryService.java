package com.foodie.delivery_service.service;


import com.foodie.delivery_service.client.OrderServiceClient;
import com.foodie.common.events.DeliveryEvent;
import com.foodie.common.events.PaymentCompletedEvent;
import com.foodie.delivery_service.model.Delivery;
import com.foodie.delivery_service.model.DeliveryPartner;
import com.foodie.delivery_service.repository.DeliveryPartnerRepository;
import com.foodie.delivery_service.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
    private final OrderServiceClient orderClient;

    private static final String TOPIC = "delivery-events";

    // Assign partner for an order automatically
    public Delivery assignForOrder(PaymentCompletedEvent event) {
        // find first available partner
        Optional<DeliveryPartner> partnerOpt = partnerRepository.findByAvailableTrue().stream().findFirst();

        // fetch order/customer info from order service if missing
        String customerEmail = event.getCustomerEmail();
        String customerPhone = event.getCustomerPhone();

        if ((customerEmail == null || customerEmail.isBlank()) || (customerPhone == null || customerPhone.isBlank())) {
            try {
                OrderServiceClient.OrderDto dto = orderClient.getOrder(event.getOrderUuid());
                if (dto != null) {
                    if (dto.customerEmail() != null) customerEmail = dto.customerEmail();
                    if (dto.customerPhone() != null) customerPhone = dto.customerPhone();
                }
            } catch (Exception ex) {
                // ignore if order service unavailable; continue with available data
            }
        }

        Delivery d = Delivery.builder()
                .orderUuid(event.getOrderUuid())
                .status("ASSIGNED")
                .assignedAt(Instant.now())
                .updatedAt(Instant.now())
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .build();

        if (partnerOpt.isPresent()) {
            DeliveryPartner p = partnerOpt.get();
            p.setAvailable(false);
            partnerRepository.save(p);

            d.setPartnerId(p.getId());
            d.setDeliveryPersonEmail(p.getPhone()); // or p.name/email if present
        } else {
            // no partner available: we still create delivery record with partnerId null
        }

        Delivery saved = deliveryRepository.save(d);

        publishDeliveryEvent(saved);
        return saved;
    }

    public Delivery manualAssign(Delivery delivery) {
        delivery.setAssignedAt(Instant.now());
        delivery.setUpdatedAt(Instant.now());
        if (delivery.getPartnerId() != null) {
            partnerRepository.findById(delivery.getPartnerId()).ifPresent(p -> {
                if (!p.isAvailable()) throw new RuntimeException("Partner already busy!");
                p.setAvailable(false);
                partnerRepository.save(p);
                delivery.setDeliveryPersonEmail(p.getPhone());
            });
        }
        Delivery saved = deliveryRepository.save(delivery);
        publishDeliveryEvent(saved);
        return saved;
    }

    public Delivery updateStatus(String deliveryId, String status) {
        Delivery d = deliveryRepository.findById(deliveryId).orElseThrow(() -> new RuntimeException("Delivery not found"));
        d.setStatus(status);
        d.setUpdatedAt(Instant.now());
        Delivery updated = deliveryRepository.save(d);
        if ("DELIVERED".equalsIgnoreCase(status) && d.getPartnerId() != null) {
            partnerRepository.findById(d.getPartnerId()).ifPresent(p -> {
                p.setAvailable(true);
                partnerRepository.save(p);
            });
        }
        publishDeliveryEvent(updated);
        return updated;
    }

    private void publishDeliveryEvent(Delivery d) {
        DeliveryEvent evt = DeliveryEvent.builder()
                .orderUuid(d.getOrderUuid())
                .status(d.getStatus())
                .deliveryPersonEmail(d.getDeliveryPersonEmail())
                .partnerId(d.getPartnerId())
                .customerEmail(d.getCustomerEmail())
                .customerPhone(d.getCustomerPhone())
                .timestamp(System.currentTimeMillis())
                .build();
        kafkaTemplate.send(TOPIC, d.getOrderUuid(), evt);
    }
}

