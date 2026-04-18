package com.furniro.OrderService.service;

import org.springframework.stereotype.Service;

import com.furniro.OrderService.database.repository.order.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

}