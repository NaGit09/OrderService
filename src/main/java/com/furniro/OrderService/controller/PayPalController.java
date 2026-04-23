package com.furniro.OrderService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.furniro.OrderService.service.PayPalService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PayPalController {

    private final PayPalService payPalService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> body) {
        String totalAmount = body.get("totalAmount");
        String currency = body.get("currency");
        return ResponseEntity.ok(payPalService.createOrder(totalAmount, currency));
    }

    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(@RequestBody Map<String, String> body) {
        String orderId = body.get("orderID");
        return ResponseEntity.ok(payPalService.captureOrder(orderId));
    }
}