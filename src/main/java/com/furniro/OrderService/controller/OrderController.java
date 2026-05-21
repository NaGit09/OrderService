package com.furniro.OrderService.controller;

import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.req.CreateOrderReq;
import com.furniro.OrderService.dto.req.UpdateStatusOrder;
import com.furniro.OrderService.service.OrderService;
import com.furniro.OrderService.utils.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
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

    // ADMIN API

    @GetMapping("/admin")
    public ResponseEntity<AType> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Integer userID) {
        return orderService.getAllOrdersForAdmin(page, size, status, userID);
    }

    @GetMapping("/admin/{orderID}")
    public ResponseEntity<AType> getOrderDetailsForAdmin(@PathVariable Integer orderID) {
        return orderService.getOrderDetailsForAdmin(orderID);
    }

    @PatchMapping("/admin/status")
    public ResponseEntity<AType> changeStatusOrderForAdmin(@RequestBody UpdateStatusOrder updateStatusOrder) {
        return orderService.changeStatusOrder(updateStatusOrder);
    }
}

