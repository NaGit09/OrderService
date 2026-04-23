package com.furniro.OrderService.dto.req;


import lombok.Data;

import java.util.List;

import com.furniro.OrderService.utils.enums.OrderStatus;
import com.furniro.OrderService.utils.enums.PaymentMethod;
import com.furniro.OrderService.utils.enums.PaymentStatus;

@Data
public class CreateOrderReq {
    private Integer userID;
    private double totalAmount;
    private String note;
    private int shippingFee;
    private OrderStatus orderStatus;
    private String address;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private String currency;

    private List<OrderItemReq> orderItems;

}