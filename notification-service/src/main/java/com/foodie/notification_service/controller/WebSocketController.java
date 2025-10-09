package com.foodie.notification_service.controller;


import com.foodie.common.events.DeliveryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebSocketController {

    @MessageMapping("/send")
    @SendTo("/topic/updates")
    public DeliveryEvent send(DeliveryEvent event) {
        return event; // echo for testing
    }
}

