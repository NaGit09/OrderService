package com.furniro.OrderService.controller;

import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.req.CreateOrderReq;
import com.furniro.OrderService.dto.req.UpdateStatusOrder;
import com.furniro.OrderService.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<AType> createOrder(@RequestBody CreateOrderReq orderReq) {
        return orderService.createOrder(orderReq);
    }

    @PatchMapping("/status")
    public ResponseEntity<AType> changeStatusOrder(@RequestBody UpdateStatusOrder updateStatusOrder) {
        return orderService.changeStatusOrder(updateStatusOrder);
    }

    @PostMapping("/capture-paypal")
    public ResponseEntity<AType> capturePayPalOrder(@RequestParam String orderId) {
        return orderService.capturePayPalOrder(orderId);
    }
}
