package com.furniro.OrderService.service.kafka;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.furniro.OrderService.service.OrderService;

import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "inventory.reservation-expired", groupId = "inventory")
    public void reservationExpired(Map<String, Object> message) {

        // convert json to map
        log.info("Received message: {}", message);

        Integer orderId = (Integer) message.get("orderID");

        String reason = (String) message.get("reason");

        if (reason.equals("Payment timeout")) {
            orderService.updateOrderStatus(orderId, "FAILED");
        }
        
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "inventory")
    public void inventoryReserved(Map<String, Object> message) {

        // convert json to map
        log.info("Received message: {}", message);

        Integer orderId = (Integer) message.get("orderID");

        String status = (String) message.get("status");

        if (status.equals("CREATED")) {
            orderService.updateOrderStatus(orderId, "APPROVED");

        } else if (status.equals("FAILED")) {
            orderService.updateOrderStatus(orderId, "FAILED");
        }

        
    }
}