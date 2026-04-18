package com.furniro.OrderService.service;

import com.furniro.OrderService.database.repository.order.OrderItemRepository;
import com.furniro.OrderService.service.kafka.KafkaProducer;
import com.furniro.OrderService.utils.enums.OrderErrorCode;
import com.furniro.OrderService.database.entity.order.Order;
import com.furniro.OrderService.database.entity.order.OrderItem;
import com.furniro.OrderService.database.entity.order.Payment;
import com.furniro.OrderService.database.repository.order.OrderRepository;
import com.furniro.OrderService.database.repository.order.PaymentRepository;
import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ApiType;
import com.furniro.OrderService.dto.req.CreateOrderReq;
import com.furniro.OrderService.dto.req.UpdateStatusOrder;
import com.furniro.OrderService.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaProducer producer;

    // create order
    @Transactional
    public ResponseEntity<AType> createOrder(CreateOrderReq orderReq) {

        // 1. Create order
        Order order = Order.builder()
                .orderNote(orderReq.getOrderNote())
                .address(orderReq.getAddress())
                .shippingFee(orderReq.getShippingFee())
                .build();

        Order orderResult = orderRepository.save(order);

        // 2. Create order item
        List<OrderItem> orderItems = orderReq.getOrderItems().stream().map(
                item -> OrderItem.builder()
                        .variant(item.getProductVariantID())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPrice())
                        .order(orderResult)
                        .build())
                .toList();

        orderItemRepository.saveAll(orderItems);

        // 3. create payment
        Payment payment = Payment.builder()
                .amount(orderResult.getTotalAmount())
                .paymentMethod(orderReq.getPaymentMethod())
                .paymentStatus(orderReq.getPaymentStatus())
                .currency(orderReq.getCurrency())
                .order(orderResult)
                .build();

        paymentRepository.save(payment);

        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Order created successfully")
                .data(true)
                .build());
    }

    // change order status
    public ResponseEntity<AType> changeStatusOrder(UpdateStatusOrder updateStatusOrder) {

        Order order = orderRepository.findById(updateStatusOrder.getOrderId()).orElseThrow(
                () -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

        order.setStatus(updateStatusOrder.getOrderStatus());

        orderRepository.save(order);

        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Order confirmed successfully")
                .data(true)
                .build());
    }

}